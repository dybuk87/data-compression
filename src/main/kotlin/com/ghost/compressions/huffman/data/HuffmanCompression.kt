package com.ghost.compressions.huffman.data

import com.ghost.compressions.bit.CompressInputStream
import com.ghost.compressions.bit.CompressOutputStream
import com.ghost.compressions.bit.divCeil
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class HuffmanCompression(
    private val codes: List<HuffmanCode>,
    private val compressed: ByteArray,
    private val bitDataLength: Int
) {
    fun serialize(): ByteArray {
        val huffmanSerialized = codes.fold(CompressOutputStream()) { acc, huffmanCode ->
            acc.insert(huffmanCode.char.toInt() and 0xFF, 8)
            acc.insert(huffmanCode.bitLength, 8)
            acc.insert(huffmanCode.encode.toInt(), huffmanCode.bitLength)
            acc
        }

        val header = ByteBuffer.allocate(8)
            .apply { this.asIntBuffer().put(huffmanSerialized.position).put(bitDataLength) }
            .array()

        return header + huffmanSerialized.toByteArray() + compressed
    }
}

class Node(var char: Byte? = null, var left: Node? = null, var right: Node? = null)

fun insert(node: Node, code: HuffmanCode, depth: Int = 0) {
    if (code.bitLength == depth) node.char = code.char
    else {
        val bit = (code.encode shr depth) and 1
        if (bit == 0L) {
            node.left = node.left ?: Node()
            insert(node.left!!, code, depth + 1)
        } else {
            node.right = node.right ?: Node()
            insert(node.right!!, code, depth + 1)
        }
    }
}

private fun getInputStreams(data : ByteArray) : Pair<CompressInputStream, CompressInputStream> {
    val (codesBitSize, dataBitSize) = ByteBuffer.wrap(data)
        .asIntBuffer()
        .let { Pair(it.get(0), it.get(1)) }

    val codeSize = codesBitSize divCeil 8
    val dataSize = dataBitSize divCeil 8

    val codeStream = CompressInputStream(data.copyOfRange(8, 8 + codeSize), codesBitSize)
    val dataStream = CompressInputStream(data.copyOfRange(8 + codeSize, 8 + codeSize + dataSize), dataBitSize)

    return Pair(codeStream, dataStream)
}

fun decodeTree(codeStream : CompressInputStream) : Node {
    val root = Node()
    while (!codeStream.eof()) {
        val char = codeStream.readInt(8).toByte()
        val bitLength = codeStream.readInt(8)
        val encode = codeStream.readLong(bitLength)
        val code = HuffmanCode(char, bitLength, encode)
        insert(root, code)
    }
    return root
}

fun decompressData(root: Node, dataStream: CompressInputStream) : ByteArray {
    val out = ByteArrayOutputStream()
    while (!dataStream.eof()) {
        var ptr: Node? = root
        while (ptr?.char == null && !dataStream.eof()) {
            ptr = if (dataStream.readBit() == 0) ptr?.left else ptr?.right
        }
        out.write((ptr?.char?.toInt() ?: 0) and 0xFF)
    }
    return out.toByteArray()
}

fun decompress(data: ByteArray): ByteArray {
    val (codeStream, dataStream) = getInputStreams(data)
    return decompressData(decodeTree(codeStream), dataStream)
}