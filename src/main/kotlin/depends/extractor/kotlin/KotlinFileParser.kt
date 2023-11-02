package depends.extractor.kotlin

import depends.entity.repo.EntityRepo
import depends.extractor.FileParser
import depends.extractor.java.JavaFileParser
import depends.extractor.java.JavaLexer
import depends.extractor.java.JavaListener
import depends.extractor.java.JavaParser
import depends.relations.IBindingResolver
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.atn.LexerATNSimulator
import org.antlr.v4.runtime.atn.ParserATNSimulator
import org.antlr.v4.runtime.atn.PredictionContextCache
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.IOException

class KotlinFileParser(entityRepo: EntityRepo?, bindingResolver: IBindingResolver) : FileParser() {
    private val javaFileParser = JavaFileParser(entityRepo, bindingResolver)
    @Throws(IOException::class)
    override fun parseFile(fileFullPath: String) {
        val input = CharStreams.fromFileName(fileFullPath)
        if (fileFullPath.endsWith(".kt")) {
            val lexer: Lexer = KotlinLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = KotlinParser(tokens)
            val bridge = KotlinListener(fileFullPath, entityRepo, bindingResolver)
            val walker = ParseTreeWalker()
            walker.walk(bridge, parser.kotlinFile())
        } else if (fileFullPath.endsWith(".java")) {
            val lexer: Lexer = JavaLexer(input)
            lexer.interpreter = LexerATNSimulator(lexer, lexer.atn, lexer.interpreter.decisionToDFA, PredictionContextCache())
            val tokens = CommonTokenStream(lexer)
            val parser = JavaParser(tokens)
            val interpreter = ParserATNSimulator(parser, parser.atn, parser.interpreter.decisionToDFA, PredictionContextCache())
            parser.interpreter = interpreter
            val bridge = JavaListener(fileFullPath, entityRepo, bindingResolver)
            val walker = ParseTreeWalker()
            try {
                walker.walk(bridge, parser.compilationUnit())
                interpreter.clearDFA()
            } catch (e: Exception) {
                System.err.println("error encountered during parse...")
                e.printStackTrace()
            }
        }
    }

    private val bindingResolver: IBindingResolver

    init {
        this.entityRepo = entityRepo
        this.bindingResolver = bindingResolver
    }
}