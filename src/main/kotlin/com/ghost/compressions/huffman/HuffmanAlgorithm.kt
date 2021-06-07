package com.ghost.compressions.huffman

import com.ghost.compressions.bit.CompressOutputStream
import com.ghost.compressions.huffman.data.HuffmanCode
import com.ghost.compressions.huffman.data.HuffmanItem
import com.ghost.compressions.huffman.data.HuffmanCompression
import com.ghost.compressions.huffman.data.combine

fun collectTextStats(data: ByteArray): Map<Byte, Long> =
    data.fold(mutableMapOf())
    { map, char ->
        val count = (map[char] ?: 0) + 1
        map[char] = count
        map
    }

fun buildNodes(data: Map<Byte, Long>): List<HuffmanItem> {
    val total = data.values.sum()
    return data
        .toList()
        .map { item -> HuffmanItem.Leaf(item.first, item.second.toDouble() / total) }
        .sortedBy { it.probability }
}

fun findInsertPosition(item: HuffmanItem, data: List<HuffmanItem>): Int {
    val result = data.indexOfFirst { it.probability > item.probability }
    return if (result != -1) result else data.size
}

fun buildHuffmanTree(data: List<HuffmanItem>): HuffmanItem =
    when (data.size) {
        0 -> HuffmanItem.Node(1.0)
        else -> {
            val workData = data.toMutableList()

            while (workData.size > 1) {
                val a = workData.removeAt(0)
                val b = workData.removeAt(0)
                val combined = combine(a, b)
                val insertPos = findInsertPosition(combined, workData)
                workData.add(insertPos, combined)
            }

            workData[0]
        }
    }

fun buildHuffmanCodes(root: HuffmanItem?, code: Long = 0, depth: Int = 0): List<HuffmanCode> =
    when (root) {
        null -> emptyList()
        is HuffmanItem.Leaf -> listOf(HuffmanCode(root.char, depth, code))
        is HuffmanItem.Node -> {
            buildHuffmanCodes(root.left, code, depth + 1) +
                    buildHuffmanCodes(root.right, code  + (1 shl depth), depth + 1)
        }
    }

fun compress(data: ByteArray, huffmanCodes: List<HuffmanCode>): CompressOutputStream {
    val searchMap = huffmanCodes.associateBy { it.char }

    return data.fold(CompressOutputStream()) { acc, char ->
        val huffmanCode = searchMap[char]!!
        acc.insert(huffmanCode.encode.toInt(), huffmanCode.bitLength)
        acc
    }
}

fun predictCompressionSize(stats: Map<Byte, Long>, codes: List<HuffmanCode>) =
    stats.entries.fold(0L) { sum, code ->
        val huffmanCode = codes.find { it.char == code.key }
        sum + code.value * huffmanCode!!.bitLength
    }

fun compress(data: ByteArray): HuffmanCompression =
    collectTextStats(data)
        .let(::buildNodes)
        .let(::buildHuffmanTree)
        .let(::buildHuffmanCodes)
        .let { codes -> codes to compress(data, codes) }
        .let { HuffmanCompression(it.first, it.second.toByteArray(), it.second.position) }
