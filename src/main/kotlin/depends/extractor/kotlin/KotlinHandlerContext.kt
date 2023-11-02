package depends.extractor.kotlin

import depends.entity.*
import depends.entity.repo.EntityRepo
import depends.extractor.java.JavaHandlerContext
import depends.extractor.kotlin.KotlinParser.ClassParameterContext
import depends.extractor.kotlin.utils.typeClassName
import depends.relations.IBindingResolver
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class KotlinHandlerContext(entityRepo: EntityRepo, bindingResolver: IBindingResolver) :
    JavaHandlerContext(entityRepo, bindingResolver) {

    val currentProperty: KotlinPropertyEntity?
        get() {
            for (i in entityStack.indices.reversed()) {
                val t = entityStack[i]
                if (t is KotlinPropertyEntity) return t
            }
            return null
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

    fun foundNewDelegation(delegationExpression: KotlinExpression) {
        val currentType = currentType()
        if (currentType is KotlinTypeEntity) {
            delegationExpression.deducedTypeDelegates.add(currentType)
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
            ?: run {
                logger.warn { "error entity class" }
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
            val newExpression = KotlinExpression(entityRepo.generateId())
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