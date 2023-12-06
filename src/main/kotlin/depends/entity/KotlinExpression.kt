package depends.entity

import depends.entity.repo.EntityRepo
import depends.relations.IBindingResolver

class KotlinExpression(
    id: Int,
    @Transient
    private var myContainer: IExtensionContainer? = null,
) : Expression(id) {
    internal var identifierPushedToParent = false
    internal var typePushedFromChild: TypeEntity? = null

    @Transient
    private var deducedTypeDelegates: ArrayList<KotlinTypeEntity>? = ArrayList()
    private val deducedTypeDelegateIds = ArrayList<Int>()
    private val myContainerId = myContainer?.id

    fun addDeducedDelegate(kotlinTypeEntity: KotlinTypeEntity) {
        if (deducedTypeDelegates == null)
            deducedTypeDelegates = ArrayList()
        deducedTypeDelegates!!.add(kotlinTypeEntity)
        deducedTypeDelegateIds.add((kotlinTypeEntity as TypeEntity).id)
    }

    override fun reload(repo: EntityRepo, expressionList: java.util.ArrayList<Expression>) {
        super.reload(repo, expressionList)
        if (deducedTypeDelegates == null)
            deducedTypeDelegates = ArrayList()
        deducedTypeDelegateIds.forEach {
            val entity = repo.getEntity(it)
            if (entity is KotlinTypeEntity) {
                deducedTypeDelegates!!.add(entity)
            }
        }
        if (myContainerId != null) {
            myContainer = repo.getEntity(myContainerId) as? IExtensionContainer
        }
    }

    override fun setType(type: TypeEntity?, referredEntity: Entity?, bindingResolver: IBindingResolver?) {
        super.setType(type, referredEntity, bindingResolver)
        // this.type was just set by super
        if (this.type != null) {
            deducedTypeDelegates?.forEach {
                it.delegateProviderType = this.type
            }
        }
    }

    override fun deduceTheParentType(bindingResolver: IBindingResolver) {
        val parent = parent
        if (!identifierPushedToParent) {
            if (type == null) return
            if (parent == null) return
            if (parent.type != null) return
            if (!parent.deriveTypeFromChild) return
            // parent's type depends on first child's type
            if (parent.deduceTypeBasedId != id) return

            // if child is a built-in/external type, then parent must also a built-in/external type.
            // in kotlin or c# expression must not be TypeEntity.buildInType in order to
            // handle extension functions.
            if (type === TypeEntity.buildInType) {
                parent.setType(TypeEntity.buildInType, TypeEntity.buildInType, bindingResolver)
                return
            }
        }

        /* if it is a logic expression, the return type/type is boolean. */
        if (parent.isLogic) {
            parent.setType(TypeEntity.buildInType, null, bindingResolver)
        } else if (parent.isDot) {
            if (parent is KotlinExpression && parent.identifierPushedToParent) {
                parent.typePushedFromChild = type
                parent.deduceTheParentType(bindingResolver)
                return
            }
            if (parent.isCall) {
                deduceParentIsFuncCall(parent, bindingResolver)
            } else {
                val variable = type.lookupVarInVisibleScope(parent.identifier)
                if (variable != null) {
                    parent.setType(variable.type, variable, bindingResolver)
                    parent.referredEntity = variable
                } else {
                    deduceParentIsFuncCall(parent, bindingResolver)
                }
            }
            if (parent.type == null) {
                parent.setType(bindingResolver.inferTypeFromName(type, parent.identifier), null, bindingResolver)
            }
        } else {
            parent.setType(type, null, bindingResolver)
        }
        if (parent.referredEntity == null) parent.referredEntity = parent.type
    }

    private fun deduceParentIsFuncCall(parent: Expression, bindingResolver: IBindingResolver) {
        val typeNow = (type ?: typePushedFromChild)!!
        val funcs = typeNow.lookupFunctionInVisibleScope(parent.identifier)
        if (myContainer != null && funcs.isEmpty()) {
            val extensionFunction =
                myContainer!!.lookupExtensionFunctionInVisibleScope(typeNow, parent.identifier, true)
            extensionFunction?.let { funcs.add(it) }
        }
        if (funcs.isEmpty()) {
            val mayBeFunction = bindingResolver.resolveName(null, parent.identifier, false)
            if (mayBeFunction is FunctionEntity) {
                if (mayBeFunction.isExtensionOfType(typeNow)) {
                    funcs.add(mayBeFunction)
                }
            }
        }
        parent.setReferredFunctions(bindingResolver, funcs)
    }

    override fun setReferredFunctions(bindingResolver: IBindingResolver, funcs: MutableList<Entity>?) {
        super.setReferredFunctions(bindingResolver, funcs)
        if (funcs.isNullOrEmpty()) return
        val func = funcs.first()
        func.apply {  }
    }
}