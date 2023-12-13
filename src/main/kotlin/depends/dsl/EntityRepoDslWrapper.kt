package depends.dsl

import depends.entity.Entity
import depends.entity.FunctionEntity
import depends.entity.GenericName
import depends.entity.TypeEntity
import depends.entity.VarEntity
import depends.entity.repo.EntityRepo
import depends.extractor.kotlin.builtins.KotlinFunctionType

class EntityRepoDslWrapper(entityRepo: EntityRepo) : EntityRepo by entityRepo {
    private var id = 0

    @DependsKotlinDsl
    operator fun Entity.unaryPlus() {
        this.setInScope(false)
        add(this)
    }

    @DependsKotlinDsl
    fun function(name: String, returnRawName: String, then: FunctionEntity.() -> Unit = {}): FunctionEntity {
        return FunctionEntity(GenericName.build(name), null, generateId(), GenericName.build(returnRawName)).apply(then)
    }

    fun nextName(): String {
        return "name${id++}"
    }

    @DependsKotlinDsl
    fun `var`(rawType: String, name: String = nextName(), then: VarEntity.() -> Unit = {}): VarEntity {
        return VarEntity(GenericName.build(name), GenericName.build(rawType), null, generateId()).apply(then)
    }

    @DependsKotlinDsl
    fun `var`(type: TypeEntity, name: String = nextName(), then: VarEntity.() -> Unit = {}): VarEntity {
        return VarEntity(GenericName.build(name), type.rawName, null, generateId()).apply {
            this.type = type
            then()
        }
    }

    @DependsKotlinDsl
    fun funcType(
        returnRawName: String, parameters: List<String>, isExtension: Boolean = false,
        then: KotlinFunctionType.() -> Unit = {},
    ): KotlinFunctionType {
        return KotlinFunctionType(
            generateId(), GenericName.build(returnRawName),
            parameters.map(GenericName::build), isExtension
        ).apply(then)
    }
}

@DependsKotlinDsl
fun EntityRepo.withBuiltInRepo(repoContext: EntityRepoDslWrapper.() -> Unit) {
    EntityRepoDslWrapper(this).repoContext()
}