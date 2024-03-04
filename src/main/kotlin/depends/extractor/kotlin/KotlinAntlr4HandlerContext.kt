package depends.extractor.kotlin

import depends.entity.*
import depends.entity.intf.IExtensionContainer
import depends.entity.repo.EntityRepo
import depends.extractor.java.JavaHandlerContext
import depends.extractor.kotlin.KotlinParser.ClassParameterContext
import depends.extractor.kotlin.KotlinParser.ReceiverTypeContext
import depends.extractor.kotlin.utils.typeClassName
import depends.extractor.kotlin.utils.usedTypeArguments
import depends.relations.IBindingResolver
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

@Deprecated("antlr4 is too slow")
class KotlinAntlr4HandlerContext(entityRepo: EntityRepo, bindingResolver: IBindingResolver) :
    JavaHandlerContext(entityRepo, bindingResolver) {

    val currentExtensionsContainer: IExtensionContainer
        get() {
            for (i in entityStack.indices.reversed()) {
                val t = entityStack[i]
                if (t is IExtensionContainer) return t
            }
            return currentTopLevelType
        }

    val currentProperty: KotlinPropertyEntity?
        get() {
            for (i in entityStack.indices.reversed()) {
                val t = entityStack[i]
                if (t is KotlinPropertyEntity) return t
            }
            return null
        }

    // kotlin顶层类型所表示的Entity，只有声明了顶层函数或顶层属性才会有此类型生成
    private val currentTopLevelType: KotlinTypeEntity by lazy {
        val typeName = "${File(currentFileEntity.qualifiedName).name.removeSuffix(".kt")}Kt"
        val result = KotlinTypeEntity(
            GenericName.build(typeName),
            currentFileEntity, idGenerator.generateId()
        )
        addToRepo(result)
        currentFileEntity.addType(result)
        currentFileEntity.getAncestorOfType(PackageEntity::class.java)?.addChild(result)
        result
    }

    fun enterGetter(currentProperty: KotlinPropertyEntity) {
        pushToStack(currentProperty.getter)
    }

    fun enterSetter(currentProperty: KotlinPropertyEntity) {
        pushToStack(currentProperty.setter)
    }

    override fun foundNewType(name: GenericName, startLine: Int): KotlinTypeEntity {
        val currentTypeEntity = KotlinTypeEntity(
            name, latestValidContainer(),
            idGenerator.generateId()
        )
        currentTypeEntity.line = startLine
        pushToStack(currentTypeEntity)
        addToRepo(currentTypeEntity)
        currentFileEntity.addType(currentTypeEntity)
        return currentTypeEntity
    }

    override fun foundMethodDeclarator(methodName: String, startLine: Int): FunctionEntity {
        return foundMethodDeclarator(methodName, startLine, null)
    }

    fun foundMethodDeclarator(methodName: String, startLine: Int, receiverType: ReceiverTypeContext?): FunctionEntity {
        val currentType = currentType()
        val functionEntity = if (currentType !is FileEntity) {
            super.foundMethodDeclarator(methodName, startLine)
        } else {
            val functionEntity = super.foundMethodDeclarator(methodName, startLine)
            currentTopLevelType.addFunction(functionEntity)
            functionEntity
        }
        if (receiverType != null) {
            functionEntity.addParameter(
                VarEntity(
                    GenericName.build("this"),
                    GenericName.build(receiverType.typeClassName),
                    functionEntity,
                    entityRepo.generateId()
                ).apply {
                    receiverType.typeModifiers()?.usedTypeArguments?.map(GenericName::build)
                        ?.let { addAnnotations(it) }
                }
            )
            functionEntity.isExtension = true
        }
        return functionEntity
    }

    fun foundNewDelegation(delegationExpression: KotlinExpression) {
        val currentType = currentType()
        if (currentType is KotlinTypeEntity) {
            delegationExpression.addDeducedDelegate(currentType)
        } else {
            logger.warn { "currentType should be ${KotlinTypeEntity::class.simpleName}" }
        }
    }

    fun foundNewPropertyInPrimaryConstructor(
        ctx: ClassParameterContext,
        typeEntity: KotlinTypeEntity,
    ) {
        val name = GenericName.build(ctx.simpleIdentifier().text)
        val type = GenericName.build(ctx.type().typeClassName)
        val isVal = ctx.VAL() != null
        val isVar = ctx.VAR() != null
        if (isVal || isVar) {
            val property = KotlinPropertyEntity(
                name,
                type,
                typeEntity,
                idGenerator,
                true, // 如果属性生命在主构造器中，属性一定有getter
                isVar
            )
            addToRepo(property)
            property.getter?.let { addToRepo(it) }
            property.setter?.let { addToRepo(it) }
            typeEntity.properties.add(property)
            lastContainer().addVar(property)
        }
    }

    fun foundNewProperty(ctx: KotlinParser.PropertyDeclarationContext): KotlinPropertyEntity? {
        val variableDeclaration = ctx.variableDeclaration()
        if (variableDeclaration == null) {
            logger.warn { "multi variable declaration does not support for class property in kotlin!" }
            return null
        }
        val currentType = currentType()
        val kotlinType = currentType as? KotlinTypeEntity
            ?: if (currentType is FileEntity) {
                currentTopLevelType
            } else {
                logger.warn { "error kotlin type entity: ${currentType.javaClass}" }
                null
            }
        // 如果编译通过，那么不带接收器的类属性一定不存在泛型参数和泛型参数约束
        // 因此只需分析属性代理或属性的getter与setter即可
        val propertyDelegate = ctx.propertyDelegate()
        if (propertyDelegate != null) {
            // TODO 分析类的属性的代理
            return null
        }
        val varType = variableDeclaration.type()
        val getter = ctx.getter()
        val setter = ctx.setter()
        val expressionContext = ctx.expression()
        val hasExpression = expressionContext != null
        val hasGetter = hasExpression || getter != null
        // 属性为可变属性则生成setter，如果发现明确的setter则生成setter
        val hasSetter = (ctx.VAR() != null && hasExpression) || setter != null
        val type = varType?.typeClassName
            ?: getter?.type()?.typeClassName
        val name = variableDeclaration.simpleIdentifier().text
        val property = KotlinPropertyEntity(
            GenericName.build(name),
            GenericName.build(type),
            currentType,
            idGenerator,
            hasGetter,
            hasSetter
        )
        if (expressionContext != null) {
            val newExpression = KotlinExpression(entityRepo.generateId(), currentExtensionsContainer)
            lastContainer().addExpression(ctx, newExpression)
            newExpression.text = ctx.text
            newExpression.setStart(ctx.start.startIndex)
            newExpression.setStop(ctx.stop.stopIndex)
            newExpression.setIdentifier(name)
            newExpression.addDeducedTypeVar(property)
        }
        lastContainer().addVar(property)
        addToRepo(property)
        kotlinType?.properties?.add(property)
        property.setter?.let { addToRepo(it) }
        property.getter?.let { addToRepo(it) }
        pushToStack(property)
        return property
    }
}