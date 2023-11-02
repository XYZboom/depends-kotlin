package depends.entity

import depends.entity.repo.IdGenerator
import java.util.*

/**
 * Kotlin property entity
 *
 * @constructor
 *
 * @param simpleName
 * @param rawType
 * @param parent
 * @param idGenerator IdGenerator because there we need getter, setter, back filed
 * or extra entity's ids
 * @param hasGetter
 * @param hasSetter
 */
class KotlinPropertyEntity(
        simpleName: GenericName,
        rawType: GenericName?,
        parent: Entity,
        idGenerator: IdGenerator,
        hasGetter: Boolean = true,
        hasSetter: Boolean = true,
) : VarEntity(simpleName, rawType, parent, idGenerator.generateId()), IKotlinJvmEntity {

    companion object {
        const val BACK_FIELD_VAR_NAME = "filed"
    }

    override var jvmName = simpleName
    val getter: FunctionEntity? =
            if (hasGetter) {
                val suffix = simpleName.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
                // 对于属性的getter，其返回值类型即为属性类型
                FunctionEntity(
                        GenericName.build("get$suffix"),
                        this,
                        idGenerator.generateId(),
                        rawType
                ).apply {
                    // 自动生成名为field的后备域
                    addVar(VarEntity(
                            GenericName.build(BACK_FIELD_VAR_NAME),
                            rawType,
                            this,
                            idGenerator.generateId()
                    ))
                }
            } else null
    val setter: FunctionEntity? =
            if (hasSetter) {
                val suffix = simpleName.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
                FunctionEntity(
                        GenericName.build("set$suffix"),
                        this,
                        idGenerator.generateId(),
                        rawType
                ).apply {
                    // 自动生成名为field的后备域
                    addVar(VarEntity(
                            GenericName.build(BACK_FIELD_VAR_NAME),
                            rawType,
                            this,
                            idGenerator.generateId()
                    ))
                }
            } else null
}