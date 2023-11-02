package depends.extractor.kotlin

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
}