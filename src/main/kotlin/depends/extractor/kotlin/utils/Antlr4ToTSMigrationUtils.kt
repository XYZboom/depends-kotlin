package depends.extractor.kotlin.utils

import org.treesitter.TSNode
import org.treesitter.TSPoint
import org.treesitter.KotlinTSNodeType.*
import org.treesitter.getChildrenOfType
import org.treesitter.getDirectChildOfType
import org.treesitter.text

fun TSNode.identifier(): TSNode? = getDirectChildOfType(Identifier)
fun TSNode.importAlias(): TSNode? = getDirectChildOfType(ImportAlias)
fun TSNode.simpleIdentifier(): TSNode? = getDirectChildOfType(SimpleIdentifier)
fun TSNode.typeParameters(): TSNode? = getDirectChildOfType(TypeParameters)
fun TSNode.type(): TSNode? = getDirectChildOfType(Type)
fun TSNode.modifiers(): TSNode? = getDirectChildOfType(Modifiers)
fun TSNode.delegationSpecifiers(): TSNode? = getDirectChildOfType(DelegationSpecifiers)
fun TSNode.delegationSpecifier(): TSNode? = getDirectChildOfType(DelegationSpecifier)
fun TSNode.constructorInvocation(): TSNode? = getDirectChildOfType(ConstructorInvocation)
fun TSNode.userType(): TSNode? = getDirectChildOfType(UserType)
fun TSNode.explicitDelegation(): TSNode? = getDirectChildOfType(ExplicitDelegation)
fun TSNode.primaryConstructor(): TSNode? = getDirectChildOfType(PrimaryConstructor)
fun TSNode.classParameters(): TSNode? = getDirectChildOfType(ClassParameters)
fun TSNode.classParameter(): TSNode? = getDirectChildOfType(ClassParameter)
fun TSNode.classBody(): TSNode? = getDirectChildOfType(ClassBody)
fun TSNode.classMemberDeclarations(): TSNode? = getDirectChildOfType(ClassMemberDeclarations)
fun TSNode.secondaryConstructor(): TSNode? = getDirectChildOfType(SecondaryConstructor)
fun TSNode.functionValueParameters(): TSNode? = getDirectChildOfType(FunctionValueParameters)
fun TSNode.parameter(): TSNode? = getDirectChildOfType(Parameter)
fun TSNode.functionType(): TSNode? = getDirectChildOfType(FunctionType)
fun TSNode.receiverType(): TSNode? = getDirectChildOfType(ReceiverType)
fun TSNode.typeModifiers(): TSNode? = getDirectChildOfType(TypeModifiers)
fun TSNode.variableDeclaration(): TSNode? = getDirectChildOfType(VariableDeclaration)
fun TSNode.multiVariableDeclaration(): TSNode? = getDirectChildOfType(MultiVariableDeclaration)
fun TSNode.propertyDelegate(): TSNode? = getDirectChildOfType(PropertyDelegate)
fun TSNode.callSuffix(): TSNode? = getDirectChildOfType(CallSuffix)
fun TSNode.getter(): TSNode? = getDirectChildOfType(Getter)
fun TSNode.setter(): TSNode? = getDirectChildOfType(Setter)
fun TSNode.expression(): TSNode? = getDirectChildOfType(Expression)
fun TSNode.classMemberDeclarationChildren() = getChildrenOfType(ClassMemberDeclaration)
fun TSNode.variableDeclarationChildren() = getChildrenOfType(VariableDeclaration)
fun TSNode.functionValueParameterChildren() = getChildrenOfType(FunctionValueParameter)
fun TSNode.parameterChildren() = getChildrenOfType(Parameter)
fun TSNode.classParameterChildren() = getChildrenOfType(ClassParameter)
fun TSNode.annotatedDelegationSpecifierChildren() = getChildrenOfType(AnnotatedDelegationSpecifier)
fun TSNode.delegationSpecifierChildren() = getChildrenOfType(DelegationSpecifier)

fun TSNode.constructorInvocationChildren() = getChildrenOfType(ConstructorInvocation)

fun TSNode.typeParameterChildren() = getChildrenOfType(TypeParameter)

inline val TSNode.start: TSPoint get() = startPoint
inline val TSPoint.line get() = row

fun TSNode.INTERFACE(fileText: String): kotlin.String? {
    for (i in 0 until childCount) {
        if (getChild(i).text(fileText) == "interface") {
            return "interface"
        }
    }
    return null
}

fun TSNode.VAR(fileText: String): kotlin.String? {
    for (i in 0 until childCount) {
        if (getChild(i).text(fileText) == "var") {
            return "var"
        }
    }
    return null
}

fun TSNode.VAL(fileText: String): kotlin.String? {
    for (i in 0 until childCount) {
        if (getChild(i).text(fileText) == "val") {
            return "val"
        }
    }
    return null
}