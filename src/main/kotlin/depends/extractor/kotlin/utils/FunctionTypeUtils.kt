package depends.extractor.kotlin.utils

import depends.entity.GenericName
import depends.extractor.kotlin.KotlinParser.FunctionTypeContext
import depends.extractor.kotlin.builtins.KotlinFunctionType

fun FunctionTypeContext.getFunctionType(id: Int): KotlinFunctionType {
    val functionTypeParams = ArrayList<GenericName>()
    val receiverType = receiverType()
    receiverType?.let {
        functionTypeParams.add(GenericName.build(it.typeClassName))
    }
    val returnType = type().typeClassName
    return KotlinFunctionType.new(id, GenericName.build(returnType), functionTypeParams, receiverType != null)
}