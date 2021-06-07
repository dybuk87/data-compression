package com.ghost.compressions.bit

import java.lang.Integer.min

class CompressInputStream(
    private val buffer: ByteArray,
    private val bitSize:Int,
    startBitPosition : Int = 0
    ) {

    private var position: Int = startBitPosition

    fun readBit() : Int {
        val index = position / 8
        val bitIndex = position % 8
        position++
        return ((buffer[index].toInt() and 0xff) shr bitIndex) and 1
    }

    fun eof() : Boolean = position >= bitSize

    fun readLong(bitCount: Int) : Long {
        val maxRead = min(32, bitCount)
        val lo = readInt(maxRead)
        val hi = if (bitCount - maxRead > 0) readInt(bitCount - 32) else 0
        return (hi.toLong() shl 32) + lo
    }

    fun readInt(bitCount: Int) : Int {
        var out = 0

        val byteIndex = position / 8
        val bitIndex = position % 8
        val bitLeft = 8 - bitIndex

        val toRead = min(bitCount, bitLeft)
        out = (buffer[byteIndex].toInt() shr bitIndex) and bitMask(toRead)

        val fullBytes = ((bitCount - toRead)/8)
        for (i in 0 until fullBytes) {
            out += (buffer[byteIndex + 1 + i].toInt() and 0xFF) shl (i * 8 + toRead)
        }

        val left = bitCount - fullBytes * 8 - toRead

        position += if (toRead <= bitLeft) toRead else 8
        position += fullBytes * 8

        if (left > 0) {
            val leftPart = buffer[position / 8].toInt() and bitMask(left)
            out += leftPart shl (toRead + fullBytes * 8)
            position += left
        }

        return out
    }

    fun seekTo(seek: Int) {
        position = seek
    }
}