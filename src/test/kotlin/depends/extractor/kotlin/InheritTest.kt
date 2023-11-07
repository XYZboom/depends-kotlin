package depends.extractor.kotlin

import depends.deptypes.DependencyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InheritTest : KotlinParserTest() {
    @BeforeEach
    fun setUp() {
        super.init()
    }

    override val myPackageName = "${packageName}.inherit.inherit"

    @Test
    fun shouldHandleInheritSuccess0() {
        val src0 = "./src/test/resources/kotlin-code-examples/inherit/inherit0/ChildInherit0.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/inherit/inherit0/ParentInherit0.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}0.ChildInherit0").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.INHERIT, relations[0].type)
        assertEquals("ParentInherit0", relations[0].entity.rawName.name)
    }

    @Test
    fun shouldHandleInheritSuccess1() {
        val src0 = "./src/test/resources/kotlin-code-examples/inherit/inherit1/ChildInherit1.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/inherit/inherit1/ParentInherit1.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/inherit/inherit1/InterfaceInherit1.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}1.ChildInherit1").relations
        assertEquals(2, relations.size)
        assertEquals(
                setOf(DependencyType.INHERIT, DependencyType.IMPLEMENT),
                relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            if (relation.type == DependencyType.INHERIT) {
                assertEquals("ParentInherit1", relation.entity.rawName.name)
            } else {
                assertEquals("InterfaceInherit1", relation.entity.rawName.name)
            }
        }
    }

    @Test
    fun shouldHandleInheritSuccess2() {
        val src0 = "./src/test/resources/kotlin-code-examples/inherit/inherit2/package0/ChildInherit2.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/inherit/inherit2/package1/ParentInherit2.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}2.package0.ChildInherit2").relations
        assertEquals(1, relations.size)
        assertEquals(DependencyType.INHERIT, relations[0].type)
        assertEquals("ParentInherit2", relations[0].entity.rawName.name)
    }

    @Test
    fun shouldHandleInheritSuccess3() {
        val src0 = "./src/test/resources/kotlin-code-examples/inherit/inherit3/ChildInherit3.kt"
        val src1 = "./src/test/resources/kotlin-code-examples/inherit/inherit3/ParentInherit3.kt"
        val src2 = "./src/test/resources/kotlin-code-examples/inherit/inherit3/InterfaceInherit3.kt"
        val parser = createParser()
        parser.parse(src0)
        parser.parse(src1)
        parser.parse(src2)
        resolveAllBindings()
        val relations = entityRepo.getEntity("${myPackageName}3.ChildInherit3").relations
        assertEquals(3, relations.size)
        assertEquals(
                setOf(DependencyType.INHERIT, DependencyType.IMPLEMENT),
                relations.map { it.type }.toSet()
        )
        for (relation in relations) {
            if (relation.type == DependencyType.INHERIT) {
                assertEquals("ParentInherit3", relation.entity.rawName.name)
            } else {
                assertTrue(relation.entity.rawName.name == "InterfaceInherit3"
                        || relation.entity.rawName.name == "Runnable")
            }
        }
    }
}