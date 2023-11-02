package `kotlin-code-examples`.inherit.inherit1

class ChildInherit1(
    private val str: String,
) : ParentInherit1(), InterfaceInherit1 {
    override fun funcInterfaceInherit1(): String {
        return str
    }
}