package depends.extractor.kotlin

import depends.extractor.java.JavaBuiltInType

class KotlinBuiltInType : JavaBuiltInType() {
    companion object {
        @JvmField
        val FUNCTION: String = Function::class.java.simpleName
    }

    override fun getBuiltInTypeName(): Array<String> {
        return arrayOf(
                *super.getBuiltInTypeName(),
                FUNCTION
        )
    }
}