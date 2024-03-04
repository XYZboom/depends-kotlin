package depends.extractor.kotlin.utils

import depends.extractor.kotlin.KotlinParser
import org.treesitter.TSNode
import org.treesitter.KotlinTSNodeType
import org.treesitter.enumType

fun KotlinParser.ClassDeclarationContext.hasSecondaryConstructor(): Boolean {
    return classBody()?.classMemberDeclarations()
            ?.classMemberDeclaration()?.any {
                it.secondaryConstructor() != null
            } ?: false
}

fun TSNode.hasSecondaryConstructor(): Boolean {
    require(enumType == KotlinTSNodeType.ClassDeclaration)
    return classBody()?.classMemberDeclarations()
        ?.classMemberDeclarationChildren()?.any {
            it.secondaryConstructor() != null
        } ?: false
}