package depends.entity

interface IKotlinExtensionsContainer {
    val id: Int
    fun setFunctionIsExtension(functionEntity: FunctionEntity)
    fun lookupExtensionFunctionInVisibleScope(type: TypeEntity, genericName: GenericName): FunctionEntity?
    // TODO 扩展属性
}