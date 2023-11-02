package depends.entity

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Serializable

private val logger = KotlinLogging.logger {}

class KotlinTypeEntity(
    simpleName: GenericName,
    parent: Entity?,
    id: Int,
) : TypeEntity(simpleName, parent, id), Serializable, IKotlinJvmEntity, IDelegateProviderType {
    @get:JvmName("myDelegateProviderType")
    var delegateProviderType: TypeEntity? = null
        internal set
    override var jvmName = simpleName
    val properties = ArrayList<KotlinPropertyEntity>()

    override fun lookupFunctionLocally(functionName: GenericName): FunctionEntity? {
        val superResult = super.lookupFunctionLocally(functionName)
        if (superResult != null) return superResult
        // kotlin自调用时一律视为kotlin属性，但java对kotlin调用时应当视为getter或setter方法的调用
        properties.forEach {
            if (it.getter?.rawName == functionName) {
                return it.getter
            } else if (it.setter?.rawName == functionName) {
                return it.setter
            }
        }
        return null
    }

    override fun getDelegateProviderType(): TypeEntity? {
        return delegateProviderType
    }
}