package depends.extractor.kotlin.utils

import org.treesitter.*
import org.treesitter.KotlinTSNodeType.*
import kotlin.String


fun TSNode.usedAnnotationNames(fileText: String): List<String> {
    try {
        return when (enumType) {
            Modifiers -> getModifiersTypeUsedAnnotationNames(fileText)
            Annotation -> getAnnotationTypeUsedAnnotationNames(fileText)
            MultiAnnotation -> getMultiAnnotationTypeUsedAnnotationNames(fileText)
            UserType -> getChildrenOfType(Identifier).map { it.text(fileText) }
            else -> throw TSParserException()
        }
    } catch (e: NullPointerException) {
        throw TSParserException(e)
    }
}

fun TSNode.usedAnnotationName(fileText:String): String  {
        try {
            return when (enumType) {
                SingleAnnotation -> getSingleAnnotationTypeUsedAnnotationName(fileText)
                UnescapedAnnotation -> getUnescapedAnnotationTypeUsedAnnotationName(fileText)
                else -> throw TSParserException()
            }
        } catch (e: NullPointerException) {
            throw TSParserException(e)
        }
    }

fun TSNode.getModifiersTypeUsedAnnotationNames(fileText: String): List<String> {
    val result = ArrayList<String>()
    val annotationList = getChildrenOfType(Annotation)
    annotationList.forEach {
        result.addAll(it.usedAnnotationNames(fileText))
    }
    return result
}

fun TSNode.getAnnotationTypeUsedAnnotationNames(fileText: String): List<String> {
    return userType()!!.usedAnnotationNames(fileText)
}

fun TSNode.getSingleAnnotationTypeUsedAnnotationName(fileText: String): String {
    return getDirectChildOfType(UnescapedAnnotation)!!.usedAnnotationName(fileText)
}

fun TSNode.getUnescapedAnnotationTypeUsedAnnotationName(fileText: String): String {
    val constructorInvocation = getDirectChildOfType(ConstructorInvocation)
    val userType = getDirectChildOfType(UserType)
    return constructorInvocation?.getDirectChildOfType(UserType)?.typeClassName(fileText)
        ?: (userType?.typeClassName(fileText)
            ?: throw TSParserException())
}

fun TSNode.getMultiAnnotationTypeUsedAnnotationNames(fileText: String): List<String> {
    return ArrayList<String>().also {
        val unescapedAnnotations = getChildrenOfType(UnescapedAnnotation)
        unescapedAnnotations.forEach { it1 ->
            it.add(it1.usedAnnotationName(fileText))
        }
    }
}