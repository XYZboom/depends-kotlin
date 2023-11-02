package depends.entity

import depends.relations.IBindingResolver

class KotlinExpression(id: Int) : Expression(id) {
    val deducedTypeDelegates = ArrayList<KotlinTypeEntity>()
    override fun setType(type: TypeEntity?, referredEntity: Entity?, bindingResolver: IBindingResolver?) {
        super.setType(type, referredEntity, bindingResolver)
        // this.type was just set by super
        if (this.type != null) {
            deducedTypeDelegates.forEach {
                it.delegateProviderType = this.type
            }
        }
    }
}