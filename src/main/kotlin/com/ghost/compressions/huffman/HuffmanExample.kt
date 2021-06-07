package com.ghost.compressions.huffman

import com.ghost.compressions.huffman.data.decompress
import com.ghost.compressions.timer
import java.io.File


fun compressionTest() {
    val data = File("huffman_test_text.txt").readText(Charsets.UTF_8)
    val result = compress(data.toByteArray(Charsets.UTF_8))
    val compressed = result.serialize()

    println("Data size: ${data.length}")
    println("Compressed size : ${compressed.size}")
    println("Compression rate : ${data.length/compressed.size}")

    val decompressedData = decompress(compressed)
    println(String(decompressedData, Charsets.UTF_8))
}

fun main() {
    timer(::compressionTest)
}