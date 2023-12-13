package depends.relations

import depends.entity.Entity
import depends.entity.GenericName
import depends.entity.TypeEntity
import depends.extractor.AbstractLangProcessor
import depends.extractor.kotlin.builtins.KotlinBuiltInEntityHandler

class KotlinBindingResolver(
    langProcessor: AbstractLangProcessor,
    isCollectUnsolvedBindings: Boolean,
    isDuckTypingDeduce: Boolean,
) : BindingResolver(langProcessor, isCollectUnsolvedBindings, isDuckTypingDeduce) {

    override fun isDelayHandleCreateExpression(): Boolean {
        return true
    }

    private val builtInTypes = HashMap<GenericName, TypeEntity>()

    override fun resolveName(fromEntity: Entity?, rawName: GenericName?, searchImport: Boolean): Entity? {
        val superResult = super.resolveName(fromEntity, rawName, searchImport)
        if (superResult === null) {
            rawName ?: return null
            if (KotlinBuiltInEntityHandler.instance == null) {
                KotlinBuiltInEntityHandler.init(repo)
            }
            return KotlinBuiltInEntityHandler.instance?.getEntity(rawName)
        }
        if (superResult === TypeEntity.buildInType) {
            rawName ?: return null
            return if (builtInTypes.containsKey(rawName)) {
                builtInTypes[rawName]
            } else {
                val typeEntity = TypeEntity(rawName, null, repo.generateId()).apply {
                    setInScope(false)
                }
                builtInTypes[rawName] = typeEntity
                repo.add(typeEntity)
                typeEntity
            }
        }
        return superResult
    }

    override fun allowExtensions(): Boolean = true
}