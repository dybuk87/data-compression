package com.ghost.compressions.huffman.data

data class HuffmanCode(
    val char: Byte,
    val bitLength: Int,
    val encode: Long
) {
    fun encodeStr() = encode.toString(2).padStart(bitLength, '0')
}