package depends.entity

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.Serializable
import java.util.WeakHashMap

private val logger = KotlinLogging.logger {}

class KotlinTypeEntity(
    simpleName: GenericName,
    parent: Entity?,
    override val id: Int,
) : TypeEntity(simpleName, parent, id), Serializable, IKotlinJvmEntity,
    IDelegateProviderType, IKotlinExtensionsContainer {

    private companion object {
        @JvmStatic
        private val extensionMap = WeakHashMap<FunctionEntity, Any>()
    }

    @get:JvmName("myDelegateProviderType")
    var delegateProviderType: TypeEntity? = null
        internal set
    override var jvmName = simpleName
    val properties = ArrayList<KotlinPropertyEntity>()
    override fun lookupFunctionLocally(functionName: GenericName?): FunctionEntity? {
        functionName ?: return null
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

    override fun setFunctionIsExtension(functionEntity: FunctionEntity) {
        extensionMap[functionEntity] = Companion
    }

    private fun isExtension(it: Entity, type: TypeEntity): Boolean {
        if (it !is FunctionEntity) return false
        if (it.parameters.isEmpty()) return false
        // 扩展函数的参数列表遵循jvm签名形式
        if (it.parameters.first().type != type) return false
        return extensionMap.contains(it)
    }

    private fun lookupExtensionFunctionInVisibleScope(
        type: TypeEntity,
        genericName: GenericName,
        searchPackage: Boolean,
    ): FunctionEntity? {
        val functionsInVisibleScope = lookupFunctionInVisibleScope(genericName)
        val function = functionsInVisibleScope?.first()
        if (functionsInVisibleScope != null
            && function is FunctionEntity
            && isExtension(function, type)
        ) {
            return function
        }
        if (searchPackage) {
            getAncestorOfType(PackageEntity::class.java).children.forEach {
                if (it !is KotlinTypeEntity) return@forEach
                if (it === this) return@forEach

                val functionEntity = it.lookupExtensionFunctionInVisibleScope(
                    type, genericName, false
                )
                if (functionEntity != null) {
                    return functionEntity
                }
            }
        }
        return null
    }

    override fun lookupExtensionFunctionInVisibleScope(
        type: TypeEntity,
        genericName: GenericName,
    ): FunctionEntity? = lookupExtensionFunctionInVisibleScope(type, genericName, true)
}