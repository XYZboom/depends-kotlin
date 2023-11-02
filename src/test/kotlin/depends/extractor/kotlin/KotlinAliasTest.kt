package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinAliasTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() = super.init()

    override val myPackageName = "${packageName}.alias.alias"

    @Test
    fun shouldAliasSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/alias/alias0/package0/ChildAlias0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/alias/alias0/package1/ParentAlias0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.package0.ChildAlias0").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.INHERIT, relations[0].type)
        assertEquals("ParentAlias0", relations[0].entity.rawName.name)
    }

    @Test
    fun shouldAliasSuccess1() {
        val src0 = "./src/test/resources/kotlin-code-examples/alias/alias1/package0/ChildAlias1.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/alias/alias1/package1/ParentAlias1.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}1.package0.ChildAlias1").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.INHERIT, relations[0].type)
        assertEquals("ParentAlias1", relations[0].entity.rawName.name)
    }

    @Test
    fun shouldAliasSuccess2() {
        val src0 = "./src/test/resources/kotlin-code-examples/alias/alias2/package0/ChildAlias2.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/alias/alias2/package1/ParentAlias2.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}2.package0.ChildAlias2").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.INHERIT, relations[0].type)
        assertEquals("ParentAlias2", relations[0].entity.rawName.name)
    }
}