package org.treesitter

fun TSNode.getDirectChildOfType(type: KotlinTSNodeType): TSNode? {
    for (i in 0 until childCount) {
        val child = getChild(i)
        if (child.enumType == type) {
            return child
        }
    }
    return null
}

fun TSNode.getChildrenOfType(type: KotlinTSNodeType): List<TSNode> {
    val result = ArrayList<TSNode>()
    for (i in 0 until childCount) {
        val child = getChild(i)
        if (child.enumType == type) {
            result.add(child)
        }
    }
    return result
}