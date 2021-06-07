package com.ghost.compressions.huffman.data

sealed class HuffmanItem(val probability: Double) {
    class Node(probability: Double, val left: HuffmanItem? = null, val right: HuffmanItem? = null) :
        HuffmanItem(probability)

    class Leaf(val char: Byte, probability: Double) : HuffmanItem(probability)
}

fun combine(a: HuffmanItem, b: HuffmanItem) =
    HuffmanItem.Node(a.probability + b.probability, a, b)

fun findChar(char: Byte, root: HuffmanItem?): HuffmanItem? =
    when (root) {
        null -> null
        is HuffmanItem.Leaf -> if (root.char == char) root else null
        is HuffmanItem.Node -> findChar(char, root.left) ?: findChar(char, root.right)
    }