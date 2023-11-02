package depends.extractor.kotlin.utils

import depends.extractor.kotlin.KotlinParser

fun KotlinParser.ClassDeclarationContext.hasSecondaryConstructor(): Boolean {
    return classBody()?.classMemberDeclarations()
            ?.classMemberDeclaration()?.any {
                it.secondaryConstructor() != null
            } ?: false
}