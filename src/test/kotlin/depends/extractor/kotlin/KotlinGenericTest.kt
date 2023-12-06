package depends.extractor.kotlin

import depends.deptypes.DependencyType
import depends.entity.TypeEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinGenericTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() {
        super.init()
    }

    override val myPackageName = "generic.generic"

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

    @Test
    fun explicitGenericArgumentCallShouldSuccess1() {
        val srcFiles = listOf(
            "./src/test/resources/kotlin-code-examples/generic/generic1/ClassGeneric1.kt",
            "./src/test/resources/kotlin-code-examples/generic/generic1/ProviderGeneric1.kt",
            "./src/test/resources/kotlin-code-examples/generic/generic1/UserGeneric1.kt"
        )
        val parser = createParser()
        srcFiles.forEach(parser::parse)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}1.userFunc").relations
        assertEquals(3, relations.size)
        assertEquals(
            listOf(DependencyType.CALL, DependencyType.CREATE, DependencyType.RETURN),
            relations.map { it.type }
        )
        for (relation in relations) {
            when (relation.type) {
                DependencyType.RETURN, DependencyType.CREATE -> {
                    assertEquals("ClassGeneric1", relation.entity.rawName.name)
                }

                DependencyType.CALL -> {
                    assertEquals("func", relation.entity.rawName.name)
                }
            }

        }
    }

    @Test
    fun implicitGenericArgumentCallShouldSuccess2() {
        val srcFiles = listOf(
            "./src/test/resources/kotlin-code-examples/generic/generic2/ClassGeneric2.kt",
            "./src/test/resources/kotlin-code-examples/generic/generic2/ProviderGeneric2.kt",
            "./src/test/resources/kotlin-code-examples/generic/generic2/UserGeneric2.kt"
        )
        val parser = createParser()
        srcFiles.forEach(parser::parse)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}2.userFunc").relations
        assertEquals(3, relations.size)
        assertEquals(
            listOf(DependencyType.CALL, DependencyType.CREATE, DependencyType.RETURN),
            relations.map { it.type }
        )
        for (relation in relations) {
            when (relation.type) {
                DependencyType.RETURN, DependencyType.CREATE -> {
                    assertEquals("ClassGeneric2", relation.entity.rawName.name)
                }

                DependencyType.CALL -> {
                    assertEquals("func", relation.entity.rawName.name)
                }
            }

        }
    }
}