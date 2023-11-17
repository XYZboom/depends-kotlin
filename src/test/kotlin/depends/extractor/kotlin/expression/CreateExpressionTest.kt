package depends.extractor.kotlin.expression

import depends.deptypes.DependencyType
import depends.extractor.kotlin.KotlinParserTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateExpressionTest : KotlinParserTest() {
    override val myPackageName: String = "expression.create.create"

    @BeforeEach
    fun setUp() = super.init()

    @Test
    fun shouldHandleCreateWithArgsSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/expression/create/create0/ProviderCreate0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/expression/create/create0/UserCreate0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        run {
            val relations = entityRepo.getEntity("${myPackageName}0.UserCreate0.test0").relations
            Assertions.assertEquals(
                setOf(
                    DependencyType.USE, DependencyType.CONTAIN, DependencyType.CREATE
                ),
                relations.map { it.type }.toSet()
            )
            for (relation in relations) {
                when (relation.type) {
                    DependencyType.USE -> {
                        Assertions.assertEquals("providerCreate0", relation.entity.rawName.name)
                    }

                    DependencyType.CONTAIN -> {
                        Assertions.assertEquals("ProviderCreate0", relation.entity.rawName.name)
                    }

                    DependencyType.CREATE -> {
                        Assertions.assertEquals("ProviderCreate0", relation.entity.rawName.name)
                    }

                    else -> {
                        Assertions.assertTrue(false, "relations should not has type: ${relation.type}")
                    }
                }
            }
        }
    }
}