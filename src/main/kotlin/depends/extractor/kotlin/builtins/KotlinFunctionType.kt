package depends.extractor.kotlin.builtins

import depends.entity.GenericName
import depends.entity.TypeEntity

class KotlinFunctionType(
    id: Int,
    val returnRawType: GenericName,
    val parameterRawTypes: List<GenericName> = emptyList(),
    val isExtension: Boolean = false
) : TypeEntity(
    GenericName.build("KotlinFunctionType"),
    null, id
) {
    init {
        setInScope(false)
    }
}