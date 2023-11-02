package depends.extractor.kotlin

import depends.extractor.ParserTest

abstract class KotlinParserTest : ParserTest() {
    abstract val myPackageName: String
    override fun init() {
        langProcessor = KotlinProcessor()
        super.init()
    }

    open fun createParser(): KotlinFileParser {
        return KotlinFileParser(entityRepo, bindingResolver)
    }
}