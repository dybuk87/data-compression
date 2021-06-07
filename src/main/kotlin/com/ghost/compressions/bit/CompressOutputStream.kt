package com.ghost.compressions.bit

import java.lang.Integer.min
import java.lang.RuntimeException
import kotlin.experimental.or

infix fun Int.divCeil(base: Int): Int =
    (this / base) + if (this % base != 0) 1 else 0

class CompressOutputStream(preAllocSize: Int = 1024, private val reallocSize: Int = 128) {

    var buffer: ByteArray = ByteArray(preAllocSize)
    var position: Int = 0

    fun toByteArray(): ByteArray {
        val size = position divCeil 8
        return ByteArray(size).apply { buffer.copyInto(this, 0, 0, size) }
    }

    private fun allocRequired(requiredSize: Int) {
        if (buffer.size * 8 < position + requiredSize) {
            val needed = 1 + (((requiredSize + position) / 8) - buffer.size) / reallocSize
            buffer = ByteArray(buffer.size + needed * requiredSize)
                .apply { buffer.copyInto(this) }
        }
    }


    fun insert(code: Long, bitLength: Int) {
        val lo = code and 0xFFFFFFFF
        val hi = (code shr 32) and 0xFFFFFFFF
        insert(lo.toInt(), min(32, bitLength))
        if (bitLength > 32) {
            insert(hi.toInt(), bitLength - 32)
        }
    }

    fun insert(code: Int, bitLength: Int) {
        allocRequired(bitLength)
        val bitStored = (position % 8)
        val byteIndex = (position / 8)

        // we try to append bits to current position until:
        // all code is inside buffer or we are aligned to byte
        val (bitAppended, newPosition) = if (bitStored > 0) {
            val appendBitCount = min(8 - bitStored, bitLength)
            val bottomBits = code and bitMask(appendBitCount)
            buffer[byteIndex] = buffer[byteIndex] or (bottomBits shl bitStored).toByte()

            // jump to beginning of next byte,
            if (appendBitCount == 8 - bitStored) {
                Pair(appendBitCount, byteIndex * 8 + 8)
            } else {
                Pair(appendBitCount, position + appendBitCount)
            }
        } else {
            Pair(0, position)
        }

        position = newPosition

        // we fill all data or we are aligned to bytes
        val bitsLeftAfterAlign = bitLength - bitAppended

        // insert full bytes
        for (i in 0 until bitsLeftAfterAlign / 8) {
            val part = (code shr (i * 8 + bitAppended)) and 0xff
            buffer[position / 8] = part.toByte()
            position += 8
        }

        // finally insert bits left
        val lastBits = (bitsLeftAfterAlign % 8)
        if (lastBits > 0) {
            val finalData = (code shr (bitLength - lastBits)) and bitMask(lastBits)
            buffer[position / 8] = buffer[position / 8] or finalData.toByte()
            position += lastBits
        }
    }

    fun align8() {
        align(8)
    }

    fun align16() {
        align(16)
    }

    fun align32() {
        align(32)
    }

    private fun align(b: Int) {
        val alignError = position % b
        if (alignError > 0) {
            val alignNeeded = b - alignError
            allocRequired(alignNeeded)
            position += alignNeeded
        }
    }
}

fun bitMask(count: Int): Int = (1 shl count) - 1