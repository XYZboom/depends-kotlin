package depends.extractor.kotlin

import depends.deptypes.DependencyType
import depends.entity.repo.BuiltInType
import depends.extractor.AbstractLangProcessor
import depends.extractor.FileParser
import depends.extractor.java.JavaImportLookupStrategy
import depends.relations.ImportLookupStrategy

class KotlinProcessor : AbstractLangProcessor() {
    override fun supportedLanguage(): String {
        return "kotlin"
    }

    override fun fileSuffixes(): Array<String> {
        // for kotlin-jvm, support kotlin and java
        return arrayOf(".kt", ".java")
    }

    override fun getImportLookupStrategy(): ImportLookupStrategy {
        return JavaImportLookupStrategy(entityRepo)
    }

    override fun getBuiltInType(): BuiltInType {
        return KotlinBuiltInType()
    }

    override fun createFileParser(): FileParser {
        return KotlinFileParser(entityRepo, bindingResolver)
    }

    override fun isEagerExpressionResolve(): Boolean {
        return false
    }

    override fun supportedRelations(): List<String> {
        val depedencyTypes = ArrayList<String>()
        depedencyTypes.add(DependencyType.IMPORT)
        depedencyTypes.add(DependencyType.CONTAIN)
        depedencyTypes.add(DependencyType.IMPLEMENT)
        depedencyTypes.add(DependencyType.INHERIT)
        depedencyTypes.add(DependencyType.CALL)
        depedencyTypes.add(DependencyType.PARAMETER)
        depedencyTypes.add(DependencyType.RETURN)
        depedencyTypes.add(DependencyType.SET)
        depedencyTypes.add(DependencyType.CREATE)
        depedencyTypes.add(DependencyType.USE)
        depedencyTypes.add(DependencyType.DELEGATE)
        depedencyTypes.add(DependencyType.CAST)
        depedencyTypes.add(DependencyType.THROW)
        depedencyTypes.add(DependencyType.ANNOTATION)
        return depedencyTypes
    }
}