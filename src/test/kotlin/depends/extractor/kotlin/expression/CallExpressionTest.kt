package depends.extractor.kotlin.expression

import depends.deptypes.DependencyType
import depends.entity.TypeEntity
import depends.extractor.kotlin.KotlinParserTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CallExpressionTest : KotlinParserTest() {
    override val myPackageName: String = "expression.call.call"

    @BeforeEach
    fun setUp() = super.init()

    @Test
    fun shouldHandleCallSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call0/ProviderCall0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call0/TestCall0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}0.TestCall0.test0").relations
            assertEquals(
                setOf(
                    DependencyType.USE, DependencyType.CONTAIN, DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(
                            relation.entity.rawName.name == "func0"
                                    || relation.entity.rawName.name == "func1"
                        )
                    }

                    DependencyType.USE -> {
                        assertEquals(relation.entity.rawName.name, "providerCall0")
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall0", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall0", relation.entity.rawName.name)
                    }

                    else -> {
                        assertTrue(false, "relations should not has type: ${relation.type}")
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleCallWithReverseInputSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call0/ProviderCall0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call0/TestCall0.kt"
        val parser = createParser()
        // reverse input
        parser.parse(src1)
        parser.parse(src0)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}0.TestCall0.test0").relations
            assertEquals(
                setOf(
                    DependencyType.USE,
                    DependencyType.CONTAIN, DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(
                            relation.entity.rawName.name == "func0"
                                    || relation.entity.rawName.name == "func1"
                        )
                    }

                    DependencyType.USE -> {
                        assertTrue(
                            relation.entity.rawName.name == "providerCall0"
                        )
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall0", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall0", relation.entity.rawName.name)
                    }

                    else -> {
                        assertTrue(false, "relations should not has type: ${relation.type}")
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleContinuousCallSuccess1() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call1/ProviderCall1.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call1/TestCall1.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/expression/call/call1/MiddleTypeCall1.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}1.TestCall1.test0").relations
            assertEquals(
                setOf(
                    DependencyType.USE,
                    DependencyType.CONTAIN, DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(
                            relation.entity.rawName.name == "func0"
                                    || relation.entity.rawName.name == "funcInMiddleType"
                        )
                    }

                    DependencyType.USE -> {
                        assertTrue(
                            relation.entity.rawName.name == "providerCall1"
                                    || relation.entity.rawName.name == "ProviderCall1"
                                    || relation.entity.rawName.name == "MiddleTypeCall1"
                        )
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall1", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall1", relation.entity.rawName.name)
                    }

                    else -> {
                        assertTrue(false, "relations should not has type: ${relation.type}")
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleCallSuccess2() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call2/ProviderCall2.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call2/TestCall2.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/expression/call/call2/InterfaceCall2.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}2.TestCall2.test0").relations
            assertEquals(
                setOf(
                    DependencyType.USE,
                    DependencyType.CONTAIN, DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(
                            relation.entity.rawName.name == "func0"
                        )
                    }

                    DependencyType.USE -> {
                        assertTrue(
                            relation.entity.rawName.name == "providerCall2"
                                    || relation.entity.rawName.name == "ProviderCall2"
                        )
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("InterfaceCall2", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall2", relation.entity.rawName.name)
                    }

                    else -> {
                        assertTrue(false, "relations should not has type: ${relation.type}")
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleCallInStringLiteralSuccess3() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call3/ProviderCall3.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call3/TestCall3.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}3.TestCall3.test0").relations
            assertEquals(
                setOf(
                    DependencyType.USE,
                    DependencyType.CONTAIN, DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.CALL -> {
                        assertTrue(
                            relation.entity.rawName.name == "func0"
                                    || relation.entity.rawName.name == "func1"
                        )
                    }

                    DependencyType.USE -> {
                        assertTrue(
                            relation.entity.rawName.name == "providerCall3"
                                    || relation.entity.rawName.name == "str" // str is a local variable
                        )
                    }

                    DependencyType.CONTAIN -> {
                        assertEquals("ProviderCall3", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        assertEquals("ProviderCall3", relation.entity.rawName.name)
                    }

                    else -> {
                        assertTrue(false, "relations should not has type: ${relation.type}")
                    }
                }
            }
        }
    }

    @Test
    fun shouldHandleCallInTopLevelSuccess4() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call4/ProviderCall4.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call4/TestCall4.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}4.TestCall4.test0").relations
            assertEquals(1, relations.size)
            assertEquals(
                DependencyType.CALL, relations[0].type
            )
            assertEquals("func", relations[0].entity.rawName.name)
        }
    }

    @Test
    fun shouldHandleCallInTopLevelSuccess5() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call5/ProviderCall5.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call5/TestCall5.java"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}5.TestCall5.main").relations
            assertEquals(
                setOf(
                    DependencyType.RETURN, DependencyType.PARAMETER,
                    DependencyType.CALL, DependencyType.USE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.RETURN, DependencyType.PARAMETER -> {
                        assertEquals(TypeEntity.buildInType, relation.entity)
                    }

                    DependencyType.USE -> {
                        assertEquals("ProviderCall5Kt", relation.entity.rawName.name)
                    }

                    DependencyType.CALL -> {
                        assertEquals("func", relation.entity.rawName.name)
                    }
                }

            }
        }
    }

    @Test
    fun shouldHandleCallInTopLevelSuccess6() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/call/call6/ProviderCall6.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/call/call6/TestCall6.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}6.user").relations
            assertEquals(1, relations.size)
            assertEquals(
                DependencyType.CALL,
                relations[0].type
            )
            assertEquals("provider", relations[0].entity.rawName.name)
        }
    }
}