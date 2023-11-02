package `kotlin-code-examples`.expression.call.call3

class TestCall3 {
    fun test0() {
        val providerCall3 = ProviderCall3()
        val str = "${providerCall3.func0()}, ${providerCall3.func1(1)}"
        println(str)
    }
}