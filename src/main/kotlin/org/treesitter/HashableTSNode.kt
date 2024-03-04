package org.treesitter


fun TSNode.hashable(): HashableTSNode {
    return HashableTSNode(this)
}

class HashableTSNode(
    val node: TSNode
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashableTSNode

        return str == other.str
    }

    override fun hashCode(): Int {
        return str.hashCode()
    }

    val str
        get() = "${node.type}: ${node.startPoint.row}:${node.startPoint.column}-" +
                "${node.endPoint.row}-${node.endPoint.column}"

    override fun toString(): String {
        return "$node"
    }
}