package depends.extractor.kotlin.extension

import depends.deptypes.DependencyType
import depends.extractor.kotlin.KotlinParserTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinExtFuncTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() = init()
    override val myPackageName = "extension.function.function"

    @Test
    fun shouldHandleExtFuncSuccess0() {
        val srcs = arrayOf(
            "./src/test/resources/kotlin-code-examples/extension/function/function0/MyClassExtFunc0.kt",
            "./src/test/resources/kotlin-code-examples/extension/function/function0/ProviderExtFunc0.kt",
            "./src/test/resources/kotlin-code-examples/extension/function/function0/UserExtFunc0.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.UserExtFunc0.test").relations
        assertEquals(
            setOf(
                DependencyType.USE, DependencyType.CONTAIN,
                DependencyType.CALL, DependencyType.CREATE
            ),
            relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            when (relation.type) {
                DependencyType.USE -> {
                    assertEquals("myClassExtFunc0", relation.entity.rawName.name)
                }

                DependencyType.CONTAIN, DependencyType.CREATE -> {
                    assertEquals("MyClassExtFunc0", relation.entity.rawName.name)
                }

                DependencyType.CALL -> {
                    assertEquals("func", relation.entity.rawName.name)
                }
            }
        }
    }

    @Test
    fun shouldHandleCallChainExtFuncSuccess1() {
        val srcs = arrayOf(
            "./src/test/resources/kotlin-code-examples/extension/function/function1/MyClassExtFunc1.kt",
            "./src/test/resources/kotlin-code-examples/extension/function/function1/ProviderExtFunc1.kt",
            "./src/test/resources/kotlin-code-examples/extension/function/function1/UserExtFunc1.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}1.UserExtFunc1.test").relations
        assertEquals(
            setOf(
                DependencyType.USE, DependencyType.CONTAIN,
                DependencyType.CALL, DependencyType.CREATE
            ),
            relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            when (relation.type) {
                DependencyType.USE -> {
                    assertEquals("myClassExtFunc1", relation.entity.rawName.name)
                }

                DependencyType.CONTAIN, DependencyType.CREATE -> {
                    assertEquals("MyClassExtFunc1", relation.entity.rawName.name)
                }

                DependencyType.CALL -> {
                    assertTrue("func" == relation.entity.rawName.name
                            || "func1" == relation.entity.rawName.name)
                }
            }
        }
    }

    @Test
    fun shouldHandleBuiltInExtFuncSuccess2() {
        val srcs = arrayOf(
            "./src/test/resources/kotlin-code-examples/extension/function/function2/ProviderExtFunc2.kt",
            "./src/test/resources/kotlin-code-examples/extension/function/function2/UserExtFunc2.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}2.UserExtFunc2.test").relations
        assertEquals(DependencyType.CALL, relations[0].type)
        assertEquals("func", relations[0].entity.rawName.name)
    }

    @Test
    fun shouldHandleBuiltInExtFuncSuccess3() {
        val srcs = arrayOf(
            "./src/test/resources/kotlin-code-examples/extension/function/function3/package0/ProviderExtFunc3.kt",
            "./src/test/resources/kotlin-code-examples/extension/function/function3/package1/UserExtFunc3.kt",
        )
        val parser = createParser()
        srcs.forEach {
            parser.parse(it)
        }
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}3.package1.UserExtFunc3.test").relations
        assertEquals(DependencyType.CALL, relations[0].type)
        assertEquals("func", relations[0].entity.rawName.name)
    }
}