package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinGenericTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() {
        super.init()
    }

    override val myPackageName = "${packageName}.generic.generic"

    @Test
    fun genericBoundShouldSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/generic/generic0/BaseGeneric0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/generic/generic0/UseGeneric0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.UseGeneric0").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.USE, relations[0].type)
        assertEquals("BaseGeneric0", relations[0].entity.rawName.name)
    }
}