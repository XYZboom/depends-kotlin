package depends.extractor.kotlin

import org.treesitter.TSNode
import org.treesitter.KotlinTSNodeType.*
import org.treesitter.enumType
import org.treesitter.getChildrenOfType
import org.treesitter.text
import kotlin.String

object ContextHelper {
    fun getName(identifier: KotlinParser.IdentifierContext): String {
        val sb = StringBuilder()
        for (id in identifier.simpleIdentifier()) {
            if (sb.isNotEmpty()) {
                sb.append(".")
            }
            sb.append(id.text)
        }
        return sb.toString()
    }

    fun getNameOfIdentifierTypeTSNode(fileContent: String, node: TSNode): String {
        require(node.enumType == Identifier)
        val sb = StringBuilder()
        for (id in node.getChildrenOfType(SimpleIdentifier)) {
            if (sb.isNotEmpty()) {
                sb.append(".")
            }
            sb.append(id.text(fileContent))
        }
        return sb.toString()
    }
}