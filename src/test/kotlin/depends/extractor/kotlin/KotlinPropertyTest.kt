package depends.extractor.kotlin

import depends.deptypes.DependencyType
import depends.entity.TypeEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinPropertyTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() = init()
    override val myPackageName = "property.property"

    @Test
    fun shouldHandlePropertySuccess0() {
        val srcs = arrayOf(
                "./src/test/resources/kotlin-code-examples/property/property0/ProviderProperty0.kt",
                "./src/test/resources/kotlin-code-examples/property/property0/UserProperty0.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.UserProperty0.test0").relations
        assertEquals(2, relations.size)
        assertEquals(DependencyType.USE, relations[0].type)
        assertTrue(relations[0].entity.rawName.name == "string"
                || relations[0].entity.rawName.name == "providerProperty0")
    }

    @Test
    fun shouldHandlePropertySuccess1() {
        val srcs = arrayOf(
                "./src/test/resources/kotlin-code-examples/property/property1/ProviderProperty1.kt",
                "./src/test/resources/kotlin-code-examples/property/property1/UserProperty1.java",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}1.UserProperty1.main").relations
        assertEquals(
                setOf(DependencyType.RETURN, DependencyType.PARAMETER,
                        DependencyType.CALL, DependencyType.USE),
                relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            when (relation.type) {
                DependencyType.RETURN, DependencyType.PARAMETER -> {
                    assertEquals(TypeEntity.buildInType, relation.entity)
                }
                DependencyType.USE -> {
                    assertEquals("property1", relation.entity.rawName.name)
                }
                DependencyType.CALL -> {
                    assertEquals("getString", relation.entity.rawName.name)
                }
            }

        }
    }
}