package depends.extractor.kotlin.utils

import depends.extractor.kotlin.KotlinBuiltInType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.treesitter.TSNode
import org.treesitter.KotlinTSNodeType.*
import org.treesitter.KotlinTSNodeType.Annotation
import org.treesitter.enumType
import org.treesitter.getChildrenOfType
import org.treesitter.getDirectChildOfType
import org.treesitter.text
import kotlin.String


private val logger = KotlinLogging.logger {}

fun TSNode.getTypeTypeTSNodeTypeClassName(fileText: String): String {
    return if (getDirectChildOfType(FunctionType) != null) {
        KotlinBuiltInType.FUNCTION
    } else if (getDirectChildOfType(ParenthesizedType) != null) {
        getDirectChildOfType(ParenthesizedType)!!.getDirectChildOfType(Type)!!.typeClassName(fileText)
    } else if (getDirectChildOfType(NullableType) != null) {
        val nullableTypeContext = getDirectChildOfType(NullableType)!!
        val typeReference = nullableTypeContext.getDirectChildOfType(TypeReference)
        if (typeReference != null) {
            typeReference.typeClassName(fileText)
        } else {
            val parenthesizedType = nullableTypeContext.getDirectChildOfType(ParenthesizedType)
            if (parenthesizedType != null) {
                parenthesizedType.getDirectChildOfType(Type)!!.typeClassName(fileText)
            } else {
                throw TSParserException()
            }
        }
    } else if (getDirectChildOfType(TypeReference) != null) {
        getDirectChildOfType(TypeReference)!!.typeClassName(fileText)
    } else if (getDirectChildOfType(DefinitelyNonNullableType) != null) {
        // 暂时不支持kotlin的联合类型
        logger.error { "does not support deduce union type now" }
        "Object"
    } else {
        throw TSParserException()
    }
}

private fun TSNode.getReceiverTypeTypeTSNodeTypeClassName(fileText: String): String {
    return if (getDirectChildOfType(ParenthesizedType) != null) {
        getDirectChildOfType(ParenthesizedType)!!.getDirectChildOfType(Type)!!.typeClassName(fileText)
    } else if (getDirectChildOfType(NullableType) != null) {
        val nullableTypeContext = getDirectChildOfType(NullableType)!!
        if (nullableTypeContext.getDirectChildOfType(TypeReference) != null) {
            nullableTypeContext.getDirectChildOfType(TypeReference)!!.typeClassName(fileText)
        } else if (nullableTypeContext.getDirectChildOfType(ParenthesizedType) != null) {
            nullableTypeContext.getDirectChildOfType(ParenthesizedType)!!.getDirectChildOfType(Type)!!.typeClassName(fileText)
        } else {
            throw TSParserException()
        }
    } else if (getDirectChildOfType(TypeReference) != null) {
        getDirectChildOfType(TypeReference)!!.typeClassName(fileText)
    } else {
        throw TSParserException()
    }
}

fun TSNode.typeClassName(fileText: String): String {
        try {
            return when (enumType) {
                Type -> getTypeTypeTSNodeTypeClassName(fileText)
                ReceiverType -> getReceiverTypeTypeTSNodeTypeClassName(fileText)
                TypeReference -> getTypeReferenceTypeTSNodeTypeClassName(fileText)
                UserType -> getUserTypeTypeTSNodeTypeClassName(fileText)
                else -> throw TSParserException()
            }
        } catch (e: NullPointerException) {
            throw TSParserException(e)
        }
    }
fun TSNode.usedTypeArguments(fileText: String): List<String>{
        try {
            return when (enumType) {
                Type -> getTypeTypeTSNodeUsedTypeArguments(fileText)
                ParenthesizedUserType -> getParenthesizedUserTypeTypeTSNodeUsedTypeArguments(fileText)
                UserType -> getUserTypeTypeTSNodeUsedTypeArguments(fileText)
                ParenthesizedType -> getParenthesizedTypeTypeTSNodeUsedTypeArguments(fileText)
                NullableType -> getNullableTypeTypeTSNodeUsedTypeArguments(fileText)
                SimpleUserType -> getSimpleUserTypeTypeTSNodeUsedTypeArguments(fileText)
                TypeProjection -> getTypeProjectionTypeTSNodeUsedTypeArguments(fileText)
                FunctionType -> getFunctionTypeTypeTSNodeUsedTypeArguments(fileText)
                ReceiverType -> getReceiverTypeTypeTSNodeUsedTypeArguments(fileText)
                TypeModifiers -> getTypeModifiersTypeTSNodeUsedTypeArguments(fileText)
                TypeModifier -> getTypeModifierTypeTSNodeUsedTypeArguments(fileText)
                else -> throw TSParserException()
            }
        } catch (e: NullPointerException) {
            throw TSParserException(e)
        }

    }


private fun TSNode.getTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    val result = ArrayList<String>()
    if (getDirectChildOfType(TypeModifiers) != null) {
        result.addAll(getDirectChildOfType(TypeModifiers)!!.usedTypeArguments(fileText))
    }
    if (getDirectChildOfType(FunctionType) != null) {
        result.addAll(getDirectChildOfType(FunctionType)!!.usedTypeArguments(fileText))
    } else if (getDirectChildOfType(ParenthesizedType) != null) {
        result.addAll(getDirectChildOfType(ParenthesizedType)!!.usedTypeArguments(fileText))
    } else if (getDirectChildOfType(NullableType) != null) {
        result.addAll(getDirectChildOfType(NullableType)!!.usedTypeArguments(fileText))
    } else if (getDirectChildOfType(TypeReference) != null) {
        // typeReference has no usedTypeArguments, pass
    } else if (getDirectChildOfType(DefinitelyNonNullableType) != null) {
        val parenthesizedUserTypes = getDirectChildOfType(DefinitelyNonNullableType)!!
            .getChildrenOfType(ParenthesizedUserType)
        parenthesizedUserTypes.forEach {
            result.addAll(it.usedTypeArguments(fileText))
        }
    }
    return result
}

private fun TSNode.getParenthesizedUserTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    return if (getDirectChildOfType(ParenthesizedUserType) != null) {
        getDirectChildOfType(ParenthesizedUserType)!!.usedTypeArguments(fileText)
    } else if (getDirectChildOfType(UserType) != null) {
        getDirectChildOfType(UserType)!!.usedTypeArguments(fileText)
    } else {
        throw TSParserException()
    }
}

private fun TSNode.getUserTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    val result = ArrayList<String>()
    getChildrenOfType(SimpleUserType).forEach {
        result.addAll(it.usedTypeArguments(fileText))
    }
    return result
}

private fun TSNode.getParenthesizedTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    return getDirectChildOfType(Type)!!.usedTypeArguments(fileText)
}

private fun TSNode.getNullableTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    return if (getDirectChildOfType(TypeReference) != null) {
        listOf(getDirectChildOfType(TypeReference)!!.typeClassName(fileText))
    } else getDirectChildOfType(ParenthesizedType)!!.usedTypeArguments(fileText)
}

private fun TSNode.getTypeReferenceTypeTSNodeTypeClassName(fileText: String): String {
    return getDirectChildOfType(UserType)!!.typeClassName(fileText)
}

private fun TSNode.getUserTypeTypeTSNodeTypeClassName(fileText: String): String {
    return text(fileText)
    /*var r = StringBuilder()
    val simpleUserTypes = getChildrenOfType(SimpleUserType)
    for (i in simpleUserTypes.indices) {
        val dot = if (r.isEmpty()) "" else "."
        r = r.append(dot).append(simpleUserTypes[i].text(fileText))
    }
    return r.toString()*/
}

private fun TSNode.getSimpleUserTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    val result = ArrayList<String>()
    result.add(getDirectChildOfType(SimpleIdentifier)!!.text(fileText))
    getDirectChildOfType(TypeArguments)?.getChildrenOfType(TypeProjection)?.forEach {
        result.addAll(it.usedTypeArguments(fileText))
    }
    return result
}

private fun TSNode.getTypeProjectionTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    return getDirectChildOfType(Type)!!.usedTypeArguments(fileText)
}

private fun TSNode.getFunctionTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    val result = ArrayList<String>()
    getDirectChildOfType(FunctionTypeParameters)!!.getChildrenOfType(Type).forEach {
        result.addAll(it.usedTypeArguments(fileText))
    }
    getDirectChildOfType(ReceiverType)?.usedTypeArguments(fileText)?.let {
        result.addAll(it)
    }
    return result
}

private fun TSNode.getReceiverTypeTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    return if (getDirectChildOfType(ParenthesizedType) != null) {
        getDirectChildOfType(ParenthesizedType)!!.usedTypeArguments(fileText)
    } else if (getDirectChildOfType(NullableType) != null) {
        getDirectChildOfType(NullableType)!!.usedTypeArguments(fileText)
    } else if (getDirectChildOfType(TypeReference) != null) {
        listOf(getDirectChildOfType(TypeReference)!!.typeClassName(fileText))
    } else emptyList()
}

private fun TSNode.getTypeModifiersTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    val result = ArrayList<String>()
    for (typeModifier in getChildrenOfType(TypeModifier)) {
        result.addAll(typeModifier.usedTypeArguments(fileText))
    }
    return result
}

private fun TSNode.getTypeModifierTypeTSNodeUsedTypeArguments(fileText: String): List<String> {
    return getDirectChildOfType(Annotation)?.usedAnnotationNames(fileText) ?: emptyList()
}
