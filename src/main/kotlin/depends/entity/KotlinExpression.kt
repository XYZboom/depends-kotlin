package depends.entity

import depends.entity.repo.EntityRepo
import depends.relations.IBindingResolver

class KotlinExpression(
    id: Int,
    @Transient
    private var myContainer: IKotlinExtensionsContainer? = null,
) : Expression(id) {
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
            myContainer = repo.getEntity(myContainerId) as? IKotlinExtensionsContainer
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
        if (type == null) return
        if (parent == null) return
        val parent = parent
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

        /* if it is a logic expression, the return type/type is boolean. */
        if (parent.isLogic) {
            parent.setType(TypeEntity.buildInType, null, bindingResolver)
        } else if (parent.isDot) {
            if (parent.isCall) {
                val funcs = type.lookupFunctionInVisibleScope(parent.identifier) ?: ArrayList<Entity>()
                if (myContainer != null) {
                    val extensionFunction = myContainer!!.lookupExtensionFunctionInVisibleScope(type, parent.identifier)
                    extensionFunction?.let {  funcs.add(it) }
                }
                parent.setReferredFunctions(bindingResolver, funcs)
            } else {
                val variable = type.lookupVarInVisibleScope(parent.identifier)
                if (variable != null) {
                    parent.setType(variable.type, variable, bindingResolver)
                    parent.referredEntity = variable
                } else {
                    val funcs = type.lookupFunctionInVisibleScope(parent.identifier)
                    parent.setReferredFunctions(bindingResolver, funcs)
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
}