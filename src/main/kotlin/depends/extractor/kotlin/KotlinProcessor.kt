package depends.extractor.kotlin

import depends.deptypes.DependencyType
import depends.entity.repo.BuiltInType
import depends.extractor.AbstractLangProcessor
import depends.extractor.FileParser
import depends.extractor.java.JavaImportLookupStrategy
import depends.relations.IBindingResolver
import depends.relations.ImportLookupStrategy
import depends.relations.KotlinBindingResolver

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

    override fun supportedRelations(): List<String> = listOf(
        DependencyType.IMPORT,
        DependencyType.CONTAIN,
        DependencyType.IMPLEMENT,
        DependencyType.INHERIT,
        DependencyType.CALL,
        DependencyType.PARAMETER,
        DependencyType.RETURN,
        DependencyType.SET,
        DependencyType.CREATE,
        DependencyType.USE,
        DependencyType.DELEGATE,
        DependencyType.CAST,
        DependencyType.THROW,
        DependencyType.ANNOTATION,
    )

    override fun createBindingResolver(
        isCollectUnsolvedBindings: Boolean,
        isDuckTypingDeduce: Boolean,
    ): IBindingResolver {
        return KotlinBindingResolver(this, isCollectUnsolvedBindings, isDuckTypingDeduce)
    }
}