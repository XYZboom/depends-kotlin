package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinAnnotationTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() = init()
    override val myPackageName = "${packageName}.annotation.annotation"

    @Test
    fun shouldHandleAnnotationSuccess0() {
        val srcs = arrayOf(
                "./src/test/resources/kotlin-code-examples/annotation/annotation0/UsedByClassAnnotation0.kt",
                "./src/test/resources/kotlin-code-examples/annotation/annotation0/UserAnnotation0.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.UserAnnotation0").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.ANNOTATION, relations[0].type)
        assertEquals("UsedByClassAnnotation0", relations[0].entity.rawName.name)
    }

}