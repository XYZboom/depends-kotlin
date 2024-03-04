package depends.extractor.kotlin.utils

import depends.entity.GenericName
import depends.extractor.kotlin.KotlinParser.FunctionTypeContext
import depends.extractor.kotlin.builtins.KotlinFunctionType
import org.treesitter.TSNode

fun FunctionTypeContext.getFunctionType(id: Int): KotlinFunctionType {
    val functionTypeParams = ArrayList<GenericName>()
    val receiverType = receiverType()
    receiverType?.let {
        functionTypeParams.add(GenericName.build(it.typeClassName))
    }
    val returnType = type().typeClassName
    return KotlinFunctionType.new(id, GenericName.build(returnType), functionTypeParams, receiverType != null)
}

fun TSNode.getFunctionType(fileText: String, id: Int): KotlinFunctionType {
    val functionTypeParams = ArrayList<GenericName>()
    val receiverType = receiverType()
    receiverType?.let {
        functionTypeParams.add(GenericName.build(it.typeClassName(fileText)))
    }
    val returnType = type()!!.typeClassName(fileText)
    return KotlinFunctionType.new(id, GenericName.build(returnType), functionTypeParams, receiverType != null)
}