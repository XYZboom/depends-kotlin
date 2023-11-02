package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinDelegateTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() = init()
    override val myPackageName = "${packageName}.delegate.delegate"

    @Test
    fun shouldHandleDelegateAllSuccess() {
        val srcs = arrayOf(
                "./src/test/resources/kotlin-code-examples/delegate/delegate0/DelegateProvider0.kt",
                "./src/test/resources/kotlin-code-examples/delegate/delegate0/DelegateUser0.kt",
                "./src/test/resources/kotlin-code-examples/delegate/delegate0/IDelegateInterface0.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.DelegateUser0").relations
        assertEquals(
                setOf(
                        DependencyType.CALL,
                        DependencyType.IMPLEMENT,
                        DependencyType.DELEGATE,
                        DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            when (relation.type) {
                DependencyType.CALL, DependencyType.DELEGATE, DependencyType.CREATE
                -> assertEquals("DelegateProvider0", relation.entity.rawName.name)

                DependencyType.IMPLEMENT -> {
                    assertEquals("IDelegateInterface0", relation.entity.rawName.name)
                }
            }
        }
    }
}