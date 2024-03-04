package depends.extractor.kotlin

import depends.entity.*
import depends.entity.repo.EntityRepo
import depends.extractor.kotlin.context.ExpressionUsageTS
import depends.extractor.kotlin.utils.*
import depends.importtypes.ExactMatchImport
import depends.relations.IBindingResolver
import io.github.oshai.kotlinlogging.KotlinLogging
import org.treesitter.TSNode
import org.treesitter.KotlinTSNodeType.*
import org.treesitter.KotlinTSNodeType.Expression
import org.treesitter.visitor.TSListener
import org.treesitter.enumType
import org.treesitter.getDirectChildOfType
import org.treesitter.text

private val logger = KotlinLogging.logger { }

class KotlinTSListener(
    private val fileFullPath: String,
    private val entityRepo: EntityRepo,
    bindingResolver: IBindingResolver,
    val fileText: String
) : TSListener {

    private val context = KotlinTSHandlerContext(entityRepo, bindingResolver, fileText)
    private val expressionUsage: ExpressionUsageTS

    private var expressionDepth = 0

    init {

        expressionUsage = ExpressionUsageTS(context, entityRepo, bindingResolver, fileText)
    }

    companion object {
        @JvmStatic
        private fun logReceiverTypeNotSupport() {
            logger.warn { "does not support extension property now" }
        }
    }

    private fun exitLastEntity() {
        context.exitLastedEntity()
    }

    private fun enterEvery(ctx: TSNode) {
        if (ctx.enumType == Expression || expressionDepth > 0) {
            val expression = expressionUsage.foundExpression(ctx)
            if (expression != null) {
                if (ctx.parent.enumType == FunctionBody
                    && ctx.parent.parent.enumType == FunctionDeclaration
                ) {
                    expression.addDeducedTypeFunction(context.currentFunction())
                }
                if (ctx.parent.enumType == ExplicitDelegation) {
                    context.foundNewDelegation(expression)
                }
            }
        }
    }

    private fun enterEnumEntry(ctx: TSNode) {
        context.foundVarDefinition(context.currentType(), ctx.text(fileText), ctx.startPoint.row)
    }

    private fun enterExpression(ctx: TSNode) {
        expressionDepth++
    }

    private fun exitExpression(ctx: TSNode) {
        expressionDepth--
    }

    private fun enterPackageHeader(ctx: TSNode) {
        if (ctx.getDirectChildOfType(Identifier) != null) {
            context.foundNewPackage(
                ContextHelper.getNameOfIdentifierTypeTSNode(fileText, ctx.getDirectChildOfType(Identifier)!!)
            )
        }
    }

    private fun enterImportHeader(ctx: TSNode) {
        context.foundNewImport(
            ExactMatchImport(
                ContextHelper.getNameOfIdentifierTypeTSNode(
                    fileText,
                    ctx.identifier()!!
                )
            )
        )
        val alias = ctx.importAlias()
        if (alias != null) {
            context.foundNewAlias(alias.identifier()!!.text(fileText), ctx.identifier()!!.text(fileText))
        }
    }

    private fun enterTypeAlias(ctx: TSNode) {
        if (ctx.typeParameters() != null) {
            foundTypeParametersUse(ctx.typeParameters()!!)
        }
        context.foundNewAlias(ctx.identifier()!!.text(fileText), ctx.userType()!!.typeClassName(fileText))
    }

    /**
     * Enter class declaration
     * ```text
     * classDeclaration
     * : modifiers? // done
     * (CLASS | (FUN NL*)? INTERFACE)
     * NL* simpleIdentifier //done
     * (NL* typeParameters)? // done
     * (NL* primaryConstructor)? // done
     * (NL* COLON NL* delegationSpecifiers)? // done
     * (NL* typeConstraints)?
     * (NL* classBody | NL* enumClassBody)?
     * ;
     * @param ctx
     */
    private fun enterClassDeclaration(ctx: TSNode) {
        val className = ctx.identifier()!!.text(fileText)
        val type = context.foundNewType(GenericName.build(className), ctx.startPoint.row)
        val classAnnotations = ctx.modifiers()?.usedAnnotationNames(fileText)?.map(GenericName::build)
        classAnnotations?.let { type.addAnnotations(it) }
        if (ctx.typeParameters() != null) {
            foundTypeParametersUse(ctx.typeParameters()!!)
        }
        if (ctx.delegationSpecifier() != null) {
            foundDelegationSpecifierUse(ctx)
        }
        val primaryConstructor = ctx.primaryConstructor()
        if (primaryConstructor != null) {
            val method = context.foundMethodDeclarator(className, ctx.startPoint.row)
            handleClassParameters(type, method, primaryConstructor.classParameter()!!)
            method.addReturnType(context.currentType())
            val primaryConstructorAnnotations = primaryConstructor
                .modifiers()?.usedAnnotationNames(fileText)?.map(GenericName::build)
            primaryConstructorAnnotations?.let { type.addAnnotations(it) }
            // 退出主构造函数声明
            exitLastEntity()
        } else {
            // kotlin中如果不存在主构造函数，需要判断是否存在次构造函数
            // 如果二者都不存在，则需要生成默认无参数构造函数
            // 次构造函数在enterSecondaryConstructor中构造
            // 此外，接口不会生成默认的主构造函数
            if (!ctx.hasSecondaryConstructor() && ctx.INTERFACE(fileText) == null) {
                val method = context.foundMethodDeclarator(className, className, emptyList(), ctx.startPoint.row)
                method.addReturnType(context.currentType())
                // 退出主构造函数声明
                exitLastEntity()
            }
        }
    }

    private fun exitClassDeclaration(ctx: TSNode) {
        exitLastEntity()
    }

    private fun enterSecondaryConstructor(ctx: TSNode) {
        val className = context.currentType().rawName.name
        val method = context.foundMethodDeclarator(className, ctx.startPoint.row)
        handleFunctionParameter(method, ctx.functionValueParameters()!!)
        method.addReturnType(context.currentType())
        val usedAnnotationNames = ctx.modifiers()?.usedAnnotationNames(fileText)?.map(GenericName::build)
        usedAnnotationNames?.let { method.addAnnotations(it) }
    }

    private fun exitSecondaryConstructor(ctx: TSNode) {
        exitLastEntity()
    }

    /**
     * Enter object declaration
     * ```text
     * objectDeclaration
     * : modifiers? OBJECT
     * NL* simpleIdentifier // done
     * (NL* COLON NL* delegationSpecifiers)? // done
     * (NL* classBody)?
     * ;
     * ```
     * @param ctx
     */
    private fun enterObjectDeclaration(ctx: TSNode) {
        val type = context.foundNewType(GenericName.build(ctx.identifier()!!.text(fileText)), ctx.start.line)
        val usedAnnotationNames = ctx.modifiers()?.usedAnnotationNames(fileText)?.map(GenericName::build)
        if (ctx.delegationSpecifier() != null) {
            foundDelegationSpecifierUse(ctx)
        }
        usedAnnotationNames?.let { type.addAnnotations(it) }
    }

    private fun exitObjectDeclaration(ctx: TSNode) {
        exitLastEntity()
    }

    private fun enterFunctionDeclaration(ctx: TSNode) {
        val type = ctx.userType()
        val usedAnnotationNames = ctx.modifiers()?.usedAnnotationNames(fileText)?.map(GenericName::build)
        val receiverType = ctx.receiverType()
        val funcName = ctx.simpleIdentifier()!!.text(fileText)
        val functionEntity = context.foundMethodDeclarator(funcName, ctx.start.line, receiverType)
        handleFunctionParameter(functionEntity, ctx.functionValueParameters()!!)
        if (type != null) {
            functionEntity.addReturnType(GenericName.build(type.typeClassName(fileText)))
        }
        usedAnnotationNames?.let { functionEntity.addAnnotations(it) }
        ctx.typeParameters()?.let { typeParameters ->
            handleFunctionTypeParameter(functionEntity, typeParameters)
        }
    }

    private fun exitFunctionDeclaration(ctx: TSNode) {
        exitLastEntity()
    }

    private fun enterPropertyDeclaration(ctx: TSNode) {
        val currentFunction = context.currentFunction()
        if (currentFunction != null) {
            handleLocalVariable(ctx)
        } else {
            handleProperty(ctx)
        }
    }

    private fun exitPropertyDeclaration(ctx: TSNode) {
        val currentType = context.currentType()
        val currentFunction = context.currentFunction()
        if (currentType is KotlinTypeEntity && currentFunction == null) {
            exitLastEntity()
        }
    }

    private fun enterGetter(ctx: TSNode) {
        val currentProperty = context.currentProperty
        if (currentProperty != null) {
            context.enterGetter(currentProperty)
        }
    }

    private fun exitGetter(ctx: TSNode) {
        val currentProperty = context.currentProperty
        if (currentProperty != null) {
            exitLastEntity()
        }
    }

    private fun enterSetter(ctx: TSNode) {
        val currentProperty = context.currentProperty
        if (currentProperty != null) {
            context.enterSetter(currentProperty)
        }
    }

    private fun exitSetter(ctx: TSNode) {
        val currentProperty = context.currentProperty
        if (currentProperty != null) {
            exitLastEntity()
        }
    }

    /**
     * Found type parameters use
     * 将泛型的模板类型和约束类型注册到上下文
     * ```kotlin
     * class A<T1, T2: Base, in T3>
     * ```
     * 类A中，T1、T2、T3为模板类型，Base为约束类型
     * @param typeParameters
     */
    private fun foundTypeParametersUse(
        typeParameters: TSNode
    ) {
        for (i in typeParameters.typeParameterChildren().indices) {
            val typeParam = typeParameters.typeParameterChildren()[i]
            val simpleId = typeParam.simpleIdentifier()
            if (simpleId != null) {
                if (typeParam.type() != null) {
                    context.foundTypeParameters(GenericName.build(typeParam.type()!!.typeClassName(fileText)))
                }
            }
            if (typeParam.simpleIdentifier() != null) {
                context.foundTypeParameters(
                    GenericName.build(typeParam.simpleIdentifier()!!.text(fileText))
                )
            }
        }
    }

    /**
     * Found delegation specifiers use
     * ```text
     * annotatedDelegationSpecifier
     * :
     * annotation*
     * NL*
     * delegationSpecifier
     * ;
     * ```
     * @param ctx
     */
    private fun foundDelegationSpecifierUse(ctx: TSNode) {
        for (i in ctx.delegationSpecifierChildren().indices) {
            val delegationSpecifier = ctx.delegationSpecifierChildren()[i]

            /**
             * ```text
             * delegationSpecifier
             * : constructorInvocation // 构造函数调用
             * | explicitDelegation // 确实是委托
             * | userType // 实现接口
             * | functionType // 实现kotlin标准库中的函数式接口
             * | SUSPEND NL* functionType
             * ;
             * ```
             */
            val constructorInvocation = delegationSpecifier.constructorInvocation()
            if (constructorInvocation != null) {
                context.foundExtends(constructorInvocation.userType()?.typeClassName(fileText))
            }
            val userType = delegationSpecifier.userType()
            if (userType != null) {
                context.foundImplements(userType.typeClassName(fileText))
            }
            val explicitDelegation = delegationSpecifier.explicitDelegation()
            if (explicitDelegation != null) {
                if (explicitDelegation.userType() != null) {
                    context.foundImplements(explicitDelegation.userType()!!.typeClassName(fileText))
                }
                // 代理的目标对象的表达式在处理表达式时与本类型关联，此处不处理
            }
        }
    }

    private fun handleClassParameters(
        type: KotlinTypeEntity,
        method: FunctionEntity,
        ctx: TSNode,
    ) {
        for (classParameter in ctx.classParameterChildren()) {
            val varEntity = VarEntity(
                GenericName.build(classParameter.simpleIdentifier()!!.text(fileText)),
                GenericName.build(classParameter.type()!!.typeClassName(fileText)),
                method, entityRepo.generateId()
            )
            method.addParameter(varEntity)
            context.foundNewPropertyInPrimaryConstructor(classParameter, type)
        }
    }

    private fun handleFunctionParameter(method: FunctionEntity, ctx: TSNode) {
        for (param in ctx.parameterChildren()) {
            val varEntity = VarEntity(
                GenericName.build(param.simpleIdentifier()!!.text(fileText)),
                GenericName.build(param.userType()!!.typeClassName(fileText)),
                method, entityRepo.generateId()
            ).apply {
                val functionType = param.userType()!!.functionType()
                if (functionType != null) {
                    val kotlinFunctionType = functionType.getFunctionType(fileText, entityRepo.generateId())
                    if (entityRepo.getEntity(kotlinFunctionType.id) == null) {
                        entityRepo.add(kotlinFunctionType)
                        context.currentFile().addChild(kotlinFunctionType)
                        kotlinFunctionType.parent = context.currentFile()
                    }
                    this.type = kotlinFunctionType
                    this.rawType = null
                }
            }
            method.addParameter(varEntity)
        }
    }

    private fun handleFunctionTypeParameter(
        functionEntity: FunctionEntity,
        typeParameters: TSNode,
    ) {
        foundTypeParametersUse(typeParameters)
        for (typeParameter in typeParameters.typeParameterChildren()) {
            functionEntity.addTypeParameter(GenericName.build(typeParameter.simpleIdentifier()!!.text(fileText)))
        }
    }

    private fun handleLocalVariable(ctx: TSNode) {
        if (ctx.receiverType() != null) {
            logReceiverTypeNotSupport()
            return
        }
        val variableDeclaration = ctx.variableDeclaration()
        val multiVariableDeclaration = ctx.multiVariableDeclaration()
        val usedAnnotationNames = ctx.modifiers()?.usedAnnotationNames(fileText)?.map(GenericName::build)
        if (variableDeclaration != null) {
            val varEntity = handleVariableDeclaration(variableDeclaration, ctx)
            usedAnnotationNames?.let { varEntity.addAnnotations(it) }
        } else if (multiVariableDeclaration != null) {
            multiVariableDeclaration.variableDeclarationChildren().forEach {
                val varEntity = handleVariableDeclaration(it, ctx)
                usedAnnotationNames?.let { it1 -> varEntity.addAnnotations(it1) }
            }
        } else {
            throw antlr4ParserException
        }
    }

    private fun handleProperty(ctx: TSNode) {
        if (ctx.receiverType() != null) {
            logReceiverTypeNotSupport()
            return
        }
        context.foundNewProperty(ctx)
    }

    private fun enterVariableDeclaration(ctx: TSNode) {
        if (ctx.parent.enumType != ForStatement) return
        val type = ctx.type()
        if (type != null) {
            context.foundVarDefinition(
                ctx.simpleIdentifier()!!.text(fileText),
                GenericName.build(type.typeClassName(fileText)),
                type.usedTypeArguments(fileText).map(GenericName::build),
                ctx.start.line
            )
        } else {
            context.foundVarDefinition(
                context.lastContainer(),
                ctx.simpleIdentifier()!!.text(fileText),
                ctx.start.line
            )
        }
    }

    private fun handleVariableDeclaration(
        variableDeclaration: TSNode,
        ctx: TSNode,
    ): VarEntity {
        val newExpression = KotlinExpression(entityRepo.generateId(), context.currentExtensionsContainer)
        context.lastContainer().addExpression(ctx, newExpression)
        newExpression.text = ctx.text(fileText)
        newExpression.location.startRow = ctx.startPoint.row
        newExpression.location.startCol = ctx.startPoint.column
        newExpression.location.stopRow = ctx.endPoint.row
        newExpression.location.stopCol = ctx.endPoint.column
        newExpression.setIdentifier(variableDeclaration.simpleIdentifier()!!.text(fileText))
        newExpression.isSet = true
        val type = variableDeclaration.userType()
        val varEntity = if (type != null) {
            context.foundVarDefinition(
                variableDeclaration.text(fileText),
                GenericName.build(type.typeClassName(fileText)),
                type.usedTypeArguments(fileText).map(GenericName::build),
                ctx.start.line
            )
        } else {
            context.foundVarDefinition(
                context.lastContainer(),
                variableDeclaration.simpleIdentifier()!!.text(fileText),
                ctx.start.line
            )
        }
        newExpression.addDeducedTypeVar(varEntity)
        return varEntity
    }

    override fun enterNode(node: TSNode) {
        if (!node.isNamed) {
            return
        }
        enterEvery(node)
        when (node.enumType) {
            SourceFile -> context.startFile(fileFullPath)
            EnumEntry -> enterEnumEntry(node)
            Expression -> enterExpression(node)
            PackageHeader -> enterPackageHeader(node)
            ImportHeader -> enterImportHeader(node)
            TypeAlias -> enterTypeAlias(node)
            ClassDeclaration -> enterClassDeclaration(node)
            SecondaryConstructor -> enterSecondaryConstructor(node)
            ObjectDeclaration -> enterObjectDeclaration(node)
            FunctionDeclaration -> enterFunctionDeclaration(node)
            PropertyDeclaration -> enterPropertyDeclaration(node)
            Getter -> enterGetter(node)
            Setter -> enterSetter(node)
            VariableDeclaration -> enterVariableDeclaration(node)
            else -> {}
        }
    }

    override fun exitNode(node: TSNode) {
        if (!node.isNamed) {
            return
        }
        when (node.enumType) {
            SourceFile -> {}
            Expression -> exitExpression(node)
            ClassDeclaration -> exitClassDeclaration(node)
            SecondaryConstructor -> exitSecondaryConstructor(node)
            ObjectDeclaration -> exitObjectDeclaration(node)
            FunctionDeclaration -> exitFunctionDeclaration(node)
            PropertyDeclaration -> exitPropertyDeclaration(node)
            Getter -> exitGetter(node)
            Setter -> exitSetter(node)
            else -> {}
        }
    }
}