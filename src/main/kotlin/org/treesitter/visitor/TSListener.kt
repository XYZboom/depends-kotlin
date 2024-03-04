package org.treesitter.visitor

import org.treesitter.TSNode

interface TSListener {
    fun enterNode(node: TSNode)
    fun exitNode(node: TSNode)
}