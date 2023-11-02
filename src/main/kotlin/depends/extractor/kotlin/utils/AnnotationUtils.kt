package depends.extractor.kotlin.utils

import depends.extractor.kotlin.KotlinParser

val KotlinParser.ModifiersContext.usedAnnotationNames: List<String>
    get() {
        val result = ArrayList<String>()
        val annotationList = annotation()
        annotationList.forEach {
            result.addAll(it.usedAnnotationNames)
        }
        return result
    }

val KotlinParser.AnnotationContext.usedAnnotationNames: List<String>
    get() {
        val singleAnnotation = singleAnnotation()
        val multiAnnotation = multiAnnotation()
        return if (singleAnnotation != null) {
            listOf(singleAnnotation.usedAnnotationName)
        } else {
            multiAnnotation?.usedAnnotationNames
                    ?: throw parserException
        }
    }

val KotlinParser.SingleAnnotationContext.usedAnnotationName: String
    get() = unescapedAnnotation().usedAnnotationName

val KotlinParser.MultiAnnotationContext.usedAnnotationNames: List<String>
    get() = ArrayList<String>().also {
        val unescapedAnnotations = unescapedAnnotation()
        unescapedAnnotations.forEach { it1 ->
            it.add(it1.usedAnnotationName)
        }
    }

val KotlinParser.UnescapedAnnotationContext.usedAnnotationName: String
    get() {
        val constructorInvocation = constructorInvocation()
        val userType = userType()
        return constructorInvocation?.userType()?.typeClassName
                ?: (userType?.typeClassName
                        ?: throw parserException)
    }