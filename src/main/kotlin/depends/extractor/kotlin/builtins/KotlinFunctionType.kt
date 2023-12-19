package depends.extractor.kotlin.builtins

import depends.entity.GenericName
import depends.entity.TypeEntity
import depends.relations.IBindingResolver

class KotlinFunctionType private constructor(
    id: Int,
    val returnRawType: GenericName,
    val parameterRawTypes: List<GenericName> = emptyList(),
    val isExtension: Boolean = false,
) : TypeEntity(
    GenericName.build("KotlinFunctionType"),
    null, id
) {

    var returnType: TypeEntity? = null
    var parameterTypes: MutableList<TypeEntity?> = ArrayList()

    init {
        setInScope(false)
    }

    override fun inferLocalLevelEntities(bindingResolver: IBindingResolver) {
        super.inferLocalLevelEntities(bindingResolver)
        returnType = bindingResolver.inferTypeFromName(this.parent, returnRawType)
        parameterRawTypes.forEach {
            parameterTypes.add(bindingResolver.inferTypeFromName(this.parent, it))
        }
    }

    companion object {

        private val cache = HashMap<String, KotlinFunctionType>()

        fun new(
            id: Int,
            returnRawType: GenericName,
            parameterRawTypes: List<GenericName> = emptyList(),
            isExtension: Boolean = false,
        ): KotlinFunctionType {
            val sb = StringBuilder()
            sb.append(returnRawType.toString())
            parameterRawTypes.forEach(sb::append)
            sb.append(isExtension)
            val cacheKey = sb.toString()
            return if (cache.containsKey(cacheKey)) {
                cache[cacheKey]!!
            } else {
                val result = KotlinFunctionType(id, returnRawType, parameterRawTypes, isExtension)
                cache[cacheKey] = result
                result
            }
        }
    }
}
