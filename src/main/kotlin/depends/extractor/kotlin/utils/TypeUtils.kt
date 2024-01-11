package depends.extractor.kotlin.utils

import depends.extractor.kotlin.KotlinBuiltInType
import depends.extractor.kotlin.KotlinParser
import io.github.oshai.kotlinlogging.KotlinLogging


private val logger = KotlinLogging.logger {}

val parserException = IllegalStateException("error in parser. Maybe caused by g4 file changing")

val KotlinParser.TypeContext.typeClassName: String
    get() {
        return if (functionType() != null) {
            KotlinBuiltInType.FUNCTION
        } else if (parenthesizedType() != null) {
            parenthesizedType().type().typeClassName
        } else if (nullableType() != null) {
            val nullableTypeContext = nullableType()
            if (nullableTypeContext.typeReference() != null) {
                nullableTypeContext.typeReference().typeClassName
            } else if (nullableTypeContext.parenthesizedType() != null) {
                nullableTypeContext.parenthesizedType().type().typeClassName
            } else {
                throw parserException
            }
        } else if (typeReference() != null) {
            typeReference().typeClassName
        } else if (definitelyNonNullableType() != null) {
            // 暂时不支持kotlin的联合类型
            logger.error { "does not support deduce union type now" }
            "Object"
        } else {
            throw parserException
        }
    }

val KotlinParser.ReceiverTypeContext.typeClassName: String
    get() {
        return if (parenthesizedType() != null) {
            parenthesizedType().type().typeClassName
        } else if (nullableType() != null) {
            val nullableTypeContext = nullableType()
            if (nullableTypeContext.typeReference() != null) {
                nullableTypeContext.typeReference().typeClassName
            } else if (nullableTypeContext.parenthesizedType() != null) {
                nullableTypeContext.parenthesizedType().type().typeClassName
            } else {
                throw parserException
            }
        } else if (typeReference() != null) {
            typeReference().typeClassName
        } else {
            throw parserException
        }
    }

val KotlinParser.TypeContext.usedTypeArguments: List<String>
    get() {
        val result = ArrayList<String>()
        if (typeModifiers() != null) {
            result.addAll(typeModifiers().usedTypeArguments)
        }
        if (functionType() != null) {
            result.addAll(functionType().usedTypeArguments)
        } else if (parenthesizedType() != null) {
            result.addAll(parenthesizedType().usedTypeArguments)
        } else if (nullableType() != null) {
            result.addAll(nullableType().usedTypeArguments)
        } else if (typeReference() != null) {
            // typeReference has no usedTypeArguments, pass
        } else if (definitelyNonNullableType() != null) {
            val parenthesizedUserTypes = definitelyNonNullableType().parenthesizedUserType()
            parenthesizedUserTypes.forEach {
                result.addAll(it.usedTypeArguments)
            }
        }
        return result
    }

val KotlinParser.ParenthesizedUserTypeContext.usedTypeArguments: List<String>
    get() {
        return if (parenthesizedUserType() != null) {
            parenthesizedUserType().usedTypeArguments
        } else if (userType() != null) {
            userType().usedTypeArguments
        } else {
            throw parserException
        }
    }

val KotlinParser.UserTypeContext.usedTypeArguments: List<String>
    get() {
        val result = ArrayList<String>()
        simpleUserType().forEach {
            result.addAll(it.usedTypeArguments)
        }
        return result
    }

val KotlinParser.ParenthesizedTypeContext.usedTypeArguments: List<String>
    get() {
        return this.type().usedTypeArguments
    }

val KotlinParser.NullableTypeContext.usedTypeArguments: List<String>
    get() {
        return if (typeReference() != null) {
            listOf(typeReference().typeClassName)
        } else parenthesizedType().usedTypeArguments
    }

val KotlinParser.TypeReferenceContext.typeClassName: String
    get() {
        return userType().typeClassName
    }

val KotlinParser.UserTypeContext.typeClassName: String
    get() {
        var r = StringBuilder()
        for (i in simpleUserType().indices) {
            val dot = if (r.isEmpty()) "" else "."
            r = r.append(dot).append(simpleUserType(i).text)
        }
        return r.toString()
    }

val KotlinParser.SimpleUserTypeContext.usedTypeArguments: List<String>
    get() {
        val result = ArrayList<String>()
        result.add(simpleIdentifier().text)
        typeArguments()?.typeProjection()?.forEach {
            result.addAll(it.usedTypeArguments)
        }
        return result
    }

val KotlinParser.TypeProjectionContext.usedTypeArguments: List<String>
    get() {
        return this.type().usedTypeArguments
    }

val KotlinParser.FunctionTypeContext.usedTypeArguments: List<String>
    get() {
        val result = ArrayList<String>()
        functionTypeParameters().type().forEach {
            result.addAll(it.usedTypeArguments)
        }
        receiverType()?.usedTypeArguments?.let {
            result.addAll(it)
        }
        return result
    }

val KotlinParser.ReceiverTypeContext.usedTypeArguments: List<String>
    get() {
        return if (parenthesizedType() != null) {
            parenthesizedType().usedTypeArguments
        } else if (nullableType() != null) {
            nullableType().usedTypeArguments
        } else if (typeReference() != null) {
            listOf(typeReference().typeClassName)
        } else emptyList()
    }

val KotlinParser.TypeModifiersContext.usedTypeArguments: List<String>
    get() {
        val result = ArrayList<String>()
        for (typeModifier in typeModifier()) {
            result.addAll(typeModifier.usedTypeArguments)
        }
        return result
    }
val KotlinParser.TypeModifierContext.usedTypeArguments: List<String>
    get() {
        return annotation().usedAnnotationNames
    }