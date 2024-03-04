package depends.extractor.kotlin.utils

class TSParserException(override val cause: Throwable? = null) :
    IllegalStateException("error in parser. Maybe caused by tree-sitter file changing")