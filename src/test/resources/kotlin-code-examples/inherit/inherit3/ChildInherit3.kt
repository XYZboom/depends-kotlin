package `kotlin-code-examples`.inherit.inherit3

object ChildInherit3 : Runnable, ParentInherit3(), InterfaceInherit3 {
    override fun run() {
        println("run ChildInherit3")
    }
}