package depends.extractor.kotlin.context

import depends.entity.GenericName
import depends.entity.KotlinExpression
import depends.entity.repo.EntityRepo
import depends.entity.repo.IdGenerator
import depends.extractor.kotlin.KotlinTSHandlerContext
import depends.extractor.kotlin.utils.antlr4ParserException
import depends.extractor.kotlin.utils.callSuffix
import depends.extractor.kotlin.utils.typeClassName
import depends.relations.IBindingResolver
import org.treesitter.*
import org.treesitter.KotlinTSNodeType.*

class ExpressionUsageTS(
    private val context: KotlinTSHandlerContext,
    entityRepo: EntityRepo,
    private val bindingResolver: IBindingResolver,
    private val fileText: String
) {
    private val idGenerator: IdGenerator = entityRepo

    private fun findExpressionInStack(ctx: TSNode?): KotlinExpression? {
        if (ctx == null) return null
        if (ctx.type == "source_file") return null
        if (ctx.parent == null) return null
        if (context.lastContainer() == null) {
            return null
        }
        return if (context.lastContainer().expressions().containsKey(ctx.parent.hashable())) {
            val result = context.lastContainer().expressions()[ctx.parent.hashable()]
            if (result is KotlinExpression) {
                result
            } else {
                findExpressionInStack(ctx.parent)
            }
        } else {
            findExpressionInStack(ctx.parent)
        }
    }

    private fun isExpressionContext(node: TSNode): Boolean {
        val type = node.enumType
        return type == Expression
                || type == Disjunction
                || type == Conjunction
                || type == Equality
                || type == Comparison
                || type == GenericCallLikeComparison
                || type == InfixOperation
                || type == ElvisExpression
                || type == InfixFunctionCall
                || type == RangeExpression
                || type == AdditiveExpression
                || type == MultiplicativeExpression
                || type == AsExpression
                || type == PrefixUnaryExpression
                || type == PostfixUnaryExpression
                || type == PrimaryExpression
                || type == ParenthesizedExpression
                || type == LiteralConstant
                || type == StringLiteral
                || type == CallableReference
                || type == FunctionLiteral
                || type == ObjectLiteral
                || type == CollectionLiteral
                || type == ThisExpression
                || type == SuperExpression
                || type == IfExpression
                || type == WhenExpression
                || type == TryExpression
                || type == JumpExpression
                // AnnotatedLambdaContext can be considered as a parameter in a function call
                || type == AnnotatedLambda
                || type == DirectlyAssignableExpression
                || type == ParenthesizedAssignableExpression
                || type == AssignableExpression
                || type == SimpleIdentifier
                || type == CallSuffix
                || type == ValueArgument
                || type == ValueArguments
                || type == NavigationExpression
    }


    /**
     * @receiver antlr4的规则上下文
     * @return 当前表达式上下文是否是某个函数调用中的参数传递
     */
    private fun TSNode.isExplicitFunctionArgument(): Boolean {
        if (enumType == AnnotatedLambda) return true
        if (enumType == Expression) return false
        if (parent.enumType != ValueArgument) return false
        val valueArgumentsContext = parent.parent
        return when (valueArgumentsContext.parent.enumType) {
            CallSuffix -> {
                true
            }

            ConstructorInvocation,
            ConstructorDelegationCall,
            EnumEntry,
            -> {
                false
            }

            else -> {
                throw antlr4ParserException
            }
        }
    }

    fun foundExpression(ctx: TSNode): KotlinExpression? {
        if (!isExpressionContext(ctx)) {
            return null
        }
        if (context.lastContainer().containsExpression(ctx)) {
            return null
        }
        /* create expression and link it with parent*/
        val parent = findExpressionInStack(ctx)
        val expression = if (ctx.parent?.childCount == 1 && parent != null
            && parent.location.startRow == ctx.startPoint.row
            && parent.location.startCol == ctx.startPoint.column
            && parent.location.stopRow == ctx.endPoint.row
            && parent.location.stopCol == ctx.endPoint.column
        ) {
            parent
        } else {
            val newExpression = KotlinExpression(idGenerator.generateId(), context.currentExtensionsContainer)
            context.lastContainer().addExpression(ctx.hashable(), newExpression)
            newExpression.parent = parent
            newExpression.text = ctx.text(fileText)
            newExpression.location.startRow = ctx.startPoint.row
            newExpression.location.startCol = ctx.startPoint.column
            newExpression.location.stopRow = ctx.endPoint.row
            newExpression.location.stopCol = ctx.endPoint.column
            newExpression
        }
        if (ctx.isExplicitFunctionArgument()) {
            expression.parent.addCallParameter(expression)
            if (ctx.enumType != AnnotatedLambda) {
                expression.parent.addResolveFirst(expression)
            } else {
                expression.addResolveFirst(expression.parent)
                expression.isAnnotatedLambda = true
            }
        }
        tryDeduceExpression(expression, ctx)
        return expression
    }

    private fun tryDeduceExpression(expression: KotlinExpression, ctx: TSNode) {
        //如果就是自己，则无需创建新的Expression
        val booleanName = GenericName.build("boolean")
        val enumType = ctx.enumType
        if (enumType == SimpleIdentifier) {
            expression.setIdentifier(ctx.text(fileText))
        } else if (enumType == Expression) {
            if (ctx.callSuffix() != null) {
                expression.isCall = true
            }
        }
        // 注意kotlin的运算符重载，因此不能推导算术运算的类型
        when (ctx.enumType) {
            Disjunction -> {
                val conjunctions = ctx.getChildrenOfType(Conjunction)
                if (conjunctions.size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            Conjunction -> {
                if (ctx.getChildrenOfType(Equality).size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            Equality -> {
                if (ctx.getChildrenOfType(Comparison).size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            Comparison -> {
                if (ctx.getChildrenOfType(GenericCallLikeComparison).size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            InfixOperation -> {
                if (ctx.getChildrenOfType(IsOperator).isNotEmpty() || ctx.getChildrenOfType(InOperator).isNotEmpty()) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            AsExpression -> {
                val types = ctx.getChildrenOfType(Type)
                if (types.isNotEmpty()) {
                    val typeClassName = types.last().typeClassName(fileText)
                    expression.isCast = true
                    expression.setRawType(typeClassName)
                    expression.disableDriveTypeFromChild()
                }
            }

            GenericCallLikeComparison -> {
                if (ctx.getChildrenOfType(CallSuffix).isNotEmpty()) {
                    expression.isCall = true
                }
            }

            PostfixUnaryExpression -> {
                handlePostfixUnary(ctx, expression)
            }

            PrimaryExpression -> {
                if (ctx.getDirectChildOfType(SimpleIdentifier) != null && !expression.identifierPushedToParent) {
                    val name = ctx.getDirectChildOfType(SimpleIdentifier)!!.text(fileText)
                    // 在此处推导表达式时，类型系统尚未构建完毕，在框架中进行延迟推导，此处仅设置标识符
                    expression.setIdentifier(name)
                    tryPushInfoToParent(expression)
                } else if (ctx.getDirectChildOfType(StringLiteral) != null) {
                    expression.rawType = GenericName.build("String")
                }
            }

            ThisExpression -> {
                expression.setIdentifier("this")
            }

            SuperExpression -> {
                expression.setIdentifier("super")
            }

            else -> {}
        }
    }

    private fun handlePostfixUnary(ctx: TSNode, expression: KotlinExpression) {
        val primaryExpression = ctx.getDirectChildOfType(PrimaryExpression)
        val simpleIdentifier = primaryExpression?.getDirectChildOfType(SimpleIdentifier)
        if (simpleIdentifier != null) {
            expression.setIdentifier(simpleIdentifier.text(fileText))
        }
        val suffix = ctx.getDirectChildOfType(PostfixUnarySuffix)
        val navigationSuffix = suffix?.getDirectChildOfType(NavigationSuffix)
        val typeArguments = suffix?.getDirectChildOfType(TypeArguments)
        if (suffix?.getDirectChildOfType(CallSuffix) != null) {
            expression.isCall = true
        } else if (navigationSuffix != null) {
            expression.isDot = true
        }
        if (typeArguments != null) {
            for (typeProjection in typeArguments.getChildrenOfType(TypeProjection)) {
                expression.callTypeArguments.add(
                    GenericName.build(
                        typeProjection.getDirectChildOfType(Type)!!.typeClassName(fileText)
                    )
                )
            }
        }
        if (navigationSuffix?.getDirectChildOfType(SimpleIdentifier) != null) {
            expression.setIdentifier(navigationSuffix.getDirectChildOfType(SimpleIdentifier)!!.text(fileText))
        }
        tryPushInfoToParent(expression)
    }

    /**
     * 由于函数的调用延迟发生，例如表达式a.foo()解析为
     *              a.foo()
     *              |     \
     *           a.foo     ()
     *           |    \
     *           a    .foo
     * 生成的表达式树如下
     *          a.foo()
     *             |
     *          a.foo
     *            |
     *            a
     * 类型推导至a.foo时才能解析a.foo中foo的标识符为foo，此时上推标识符到父表达式
     */
    private fun tryPushInfoToParent(expression: KotlinExpression) {
        val parent = expression.parent as? KotlinExpression
        if (parent != null && (parent.isDot || parent.isCall)) {
            var target = parent
            while (target?.identifierPushedToParent == true) {
                target = target.parent as? KotlinExpression?
            }
            target?.setCaller(expression)
        }
        if (parent != null && parent.callTypeArguments.isNotEmpty()
            && expression.identifier != null
        ) {
            val parent2 = parent.parent as? KotlinExpression
            if (parent2 != null && parent2.identifier == null) {
                parent2.callTypeArguments.addAll(parent.callTypeArguments)
                parent.callTypeArguments.clear()
                parent.identifierPushedToParent = true
                expression.identifierPushedToParent = true
                parent2.identifier = expression.identifier
                expression.setIdentifierToNull()
                if (expression.isDot) {
                    parent.isDot = true
                    parent2.isDot = true
                }
            }
        } else if (parent?.isCall == true && expression.identifier != null && parent.identifier == null) {
            expression.identifierPushedToParent = true
            parent.identifier = expression.identifier
            expression.setIdentifierToNull()
            if (expression.isDot) {
                parent.isDot = true
            }
        }
    }
}


