package `kotlin-code-examples`

import kotlin.reflect.KProperty

class A(private val property: String = "132") {
    private var str: String = "123"
    val s: String = "123"
        get() {
            return "123$field"
        }

}

private operator fun String.getValue(a: A, property: KProperty<*>): String {
    return this
}

fun main(args: Array<String>) {
    val a = A("123")
//    val b = a.copy()
}