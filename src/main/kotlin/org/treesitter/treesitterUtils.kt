package org.treesitter

import org.treesitter.visitor.TSListener
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

fun TSNode.accept(listener: TSListener) {
    listener.enterNode(this)
    for (i in 0 until childCount) {
        getChild(i).accept(listener)
    }
    listener.exitNode(this)
}

private val nonastTypes = HashSet<String>()

val TSNode.enumType: KotlinTSNodeType
    get() {
        val typeStr = type.split("_").joinToString(separator = "") { s ->
            s.lowercase(Locale.getDefault())
                .replaceFirstChar {
                    if (it.isLowerCase())
                        it.titlecase(Locale.getDefault())
                    else
                        it.toString()
                }
        }
        when (typeStr) {
            "TypeIdentifier" -> return KotlinTSNodeType.Identifier
            "CallExpression" -> return KotlinTSNodeType.Expression
            "MultilineComment", "Package", "", ".", "Class", ":", "Fun",
            ",", "(", ")", "{", "}", "[", "]", "Val", "Interface",
            "Object", "Import", "As", "=", "Typealias", "LineComment",
            -> return KotlinTSNodeType.NonASTType
        }
        try {
            val valueOf = KotlinTSNodeType.valueOf(typeStr)
            return valueOf
        } catch (e: IllegalArgumentException) {
            nonastTypes.add(typeStr)
            println(nonastTypes.joinToString())
            return KotlinTSNodeType.NonASTType
        }
    }

fun TSNode.text(fileContent: String): String {
    val lines = fileContent.split(System.lineSeparator())
    val selectedLines = lines.subList(startPoint.row, endPoint.row + 1)
    val stringBuilder = StringBuilder()

    for ((index, line) in selectedLines.withIndex()) {
        val startIndex = if (index == 0) startPoint.column else 0
        val endIndex = if (index == selectedLines.size - 1) endPoint.column else line.length
        stringBuilder.append(line.substring(startIndex, endIndex))

        if (index != selectedLines.size - 1) {
            stringBuilder.append(System.lineSeparator())
        }
    }

    return stringBuilder.toString()
}