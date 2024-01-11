package depends.extractor.kotlin.context

import depends.entity.GenericName
import depends.entity.KotlinExpression
import depends.entity.repo.EntityRepo
import depends.entity.repo.IdGenerator
import depends.extractor.kotlin.KotlinHandlerContext
import depends.extractor.kotlin.KotlinParser.*
import depends.extractor.kotlin.utils.parserException
import depends.extractor.kotlin.utils.typeClassName
import depends.relations.IBindingResolver
import io.github.oshai.kotlinlogging.KotlinLogging
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext

private val logger = KotlinLogging.logger {}

class ExpressionUsage(
    private val context: KotlinHandlerContext,
    entityRepo: EntityRepo,
    private val bindingResolver: IBindingResolver,
) {
    private val idGenerator: IdGenerator = entityRepo

    private fun findExpressionInStack(ctx: RuleContext?): KotlinExpression? {
        if (ctx == null) return null
        if (ctx.parent == null) return null
        if (context.lastContainer() == null) {
            return null
        }
        return if (context.lastContainer().expressions().containsKey(ctx.parent)) {
            val result = context.lastContainer().expressions()[ctx.parent]
            if (result is KotlinExpression) {
                result
            } else {
                findExpressionInStack(ctx.parent)
            }
        } else {
            findExpressionInStack(ctx.parent)
        }
    }

    private fun isExpressionContext(ctx: ParserRuleContext): Boolean {
        return ctx is ExpressionContext
                || ctx is DisjunctionContext
                || ctx is ConjunctionContext
                || ctx is EqualityContext
                || ctx is ComparisonContext
                || ctx is GenericCallLikeComparisonContext
                || ctx is InfixOperationContext
                || ctx is ElvisExpressionContext
                || ctx is InfixFunctionCallContext
                || ctx is RangeExpressionContext
                || ctx is AdditiveExpressionContext
                || ctx is MultiplicativeExpressionContext
                || ctx is AsExpressionContext
                || ctx is PrefixUnaryExpressionContext
                || ctx is PostfixUnaryExpressionContext
                || ctx is PrimaryExpressionContext
                || ctx is ParenthesizedExpressionContext
                || ctx is LiteralConstantContext
                || ctx is StringLiteralContext
                || ctx is CallableReferenceContext
                || ctx is FunctionLiteralContext
                || ctx is ObjectLiteralContext
                || ctx is CollectionLiteralContext
                || ctx is ThisExpressionContext
                || ctx is SuperExpressionContext
                || ctx is IfExpressionContext
                || ctx is WhenExpressionContext
                || ctx is TryExpressionContext
                || ctx is JumpExpressionContext
                // AnnotatedLambdaContext can be considered as a parameter in a function call
                || ctx is AnnotatedLambdaContext
                || ctx is DirectlyAssignableExpressionContext
                || ctx is ParenthesizedAssignableExpressionContext
    }


    /**
     * @receiver antlr4的规则上下文
     * @return 当前表达式上下文是否是某个函数调用中的参数传递
     */
    private fun RuleContext.isExplicitFunctionArgument(): Boolean {
        if (this is AnnotatedLambdaContext) return true
        if (this !is ExpressionContext) return false
        if (parent !is ValueArgumentContext) return false
        val valueArgumentsContext = parent.parent as ValueArgumentsContext
        return when (valueArgumentsContext.parent) {
            is CallSuffixContext -> {
                true
            }

            is ConstructorInvocationContext,
            is ConstructorDelegationCallContext,
            is EnumEntryContext,
            -> {
                false
            }

            else -> {
                throw parserException
            }
        }
    }

    fun foundExpression(ctx: ParserRuleContext): KotlinExpression? {
        if (!isExpressionContext(ctx)) {
            return null
        }
        if (context.lastContainer().containsExpression(ctx)) {
            return null
        }
        /* create expression and link it with parent*/
        val parent = findExpressionInStack(ctx)
        val expression = if (ctx.parent?.childCount == 1 && parent != null
            && parent.location.startIndex == ctx.start.startIndex
            && parent.location.stopIndex == ctx.stop.stopIndex
        ) {
            parent
        } else {
            val newExpression = KotlinExpression(idGenerator.generateId(), context.currentExtensionsContainer)
            context.lastContainer().addExpression(ctx, newExpression)
            newExpression.parent = parent
            newExpression.text = ctx.text
            newExpression.setStart(ctx.start.startIndex)
            newExpression.setStop(ctx.stop.stopIndex)
            newExpression
        }
        if (ctx.isExplicitFunctionArgument()) {
            expression.parent.addCallParameter(expression)
            if (ctx !is AnnotatedLambdaContext) {
                expression.parent.addResolveFirst(expression)
            } else {
                expression.addResolveFirst(expression.parent)
                expression.isAnnotatedLambda = true
            }
        }
        tryDeduceExpression(expression, ctx)
        return expression
    }

    private fun tryDeduceExpression(expression: KotlinExpression, ctx: ParserRuleContext) {
        //如果就是自己，则无需创建新的Expression
        val booleanName = GenericName.build("boolean")
        // 注意kotlin的运算符重载，因此不能推导算术运算的类型
        when (ctx) {
            is DisjunctionContext -> {
                val conjunctions = ctx.conjunction()
                if (conjunctions.size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is ConjunctionContext -> {
                if (ctx.equality().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is EqualityContext -> {
                if (ctx.comparison().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is ComparisonContext -> {
                if (ctx.genericCallLikeComparison().size > 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is InfixOperationContext -> {
                if (ctx.isOperator.size >= 1 || ctx.inOperator().size >= 1) {
                    expression.rawType = booleanName
                    expression.disableDriveTypeFromChild()
                }
            }

            is AsExpressionContext -> {
                val types = ctx.type()
                if (types.size >= 1) {
                    val typeClassName = types.last().typeClassName
                    expression.isCast = true
                    expression.setRawType(typeClassName)
                    expression.disableDriveTypeFromChild()
                }
            }

            is GenericCallLikeComparisonContext -> {
                if (ctx.callSuffix().size >= 1)
                    expression.isCall = true
            }

            is PostfixUnaryExpressionContext -> {
                handlePostfixUnary(ctx, expression)
            }

            is PrimaryExpressionContext -> {
                if (ctx.simpleIdentifier() != null && !expression.identifierPushedToParent) {
                    val name = ctx.simpleIdentifier().text
                    // 在此处推导表达式时，类型系统尚未构建完毕，在框架中进行延迟推导，此处仅设置标识符
                    expression.setIdentifier(name)
                    tryPushInfoToParent(expression)
                } else if (ctx.stringLiteral() != null) {
                    expression.rawType = GenericName.build("String")
                }
            }

            is ThisExpressionContext -> {
                expression.setIdentifier("this")
            }

            is SuperExpressionContext -> {
                expression.setIdentifier("super")
            }
        }
    }

    private fun handlePostfixUnary(ctx: PostfixUnaryExpressionContext, expression: KotlinExpression) {
        val primaryExpression = ctx.primaryExpression()
        val simpleIdentifier = primaryExpression?.simpleIdentifier()
        if (simpleIdentifier != null) {
            expression.setIdentifier(simpleIdentifier.text)
        }
        val suffix = ctx.postfixUnarySuffix()
        val navigationSuffix = suffix?.navigationSuffix()
        val typeArguments = suffix?.typeArguments()
        if (suffix?.callSuffix() != null) {
            expression.isCall = true
        } else if (navigationSuffix != null) {
            expression.isDot = true
        }
        if (typeArguments != null) {
            for (typeProjection in typeArguments.typeProjection()) {
                expression.callTypeArguments.add(GenericName.build(typeProjection.type().typeClassName))
            }
        }
        if (navigationSuffix?.simpleIdentifier() != null) {
            expression.setIdentifier(navigationSuffix.simpleIdentifier().text)
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