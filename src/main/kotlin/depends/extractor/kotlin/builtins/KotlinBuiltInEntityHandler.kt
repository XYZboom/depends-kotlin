package depends.extractor.kotlin.builtins

import depends.dsl.withBuiltInRepo
import depends.entity.GenericName
import depends.entity.repo.EntityRepo

class KotlinBuiltInEntityHandler private constructor(
    entityRepo: EntityRepo,
): EntityRepo by entityRepo {
    init {
        entityRepo.withBuiltInRepo {
            +function("apply", "T") {
                addParameter(`var`("T"))
                addParameter(`var`(
                    funcType("T", listOf("T"), true)
                ))
                rawName.appendArguments(GenericName.build("T"))
                isExtension = true
            }
        }
    }

    companion object {
        var instance: KotlinBuiltInEntityHandler? = null

        fun init(entityRepo: EntityRepo) {
            instance = KotlinBuiltInEntityHandler(entityRepo)
        }
    }
}