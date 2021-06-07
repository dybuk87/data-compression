package com.ghost.compressions.bit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CompressInputStreamTest {

    @Test
    fun testReadBits() {
        val input = CompressInputStream(byteArrayOf(0b01011010, (0b11100101).toByte()), 16, 0)

        val expected = listOf(
            0, 1, 0, 1, 1, 0, 1, 0,
            1, 0, 1, 0, 0, 1, 1, 1
        )

        for (i in expected.indices) {
            Assertions.assertFalse(input.eof())
            Assertions.assertEquals(expected[i], input.readBit())
        }

        Assertions.assertTrue(input.eof())
    }

    @Test
    fun testReadInt() {
        val input = CompressInputStream(byteArrayOf(0b01011010, (0b11100101).toByte(), 0b01101001), 24, 4)

        val data = input.readInt(17)

        val expected = 0b0101 + (0b11100101 shl 4) + (0b01001 shl 12)

        Assertions.assertEquals(expected, data)
    }


    @Test
    fun testReadLong() {
        val input = CompressInputStream(byteArrayOf(0x72, (0xA8).toByte(), 0x67, 0x76, 0x32, 0x67, 0x29, 0x12, 0x34), 24, 4)

        val data1 = input.readInt(32)
        val data2 = input.readInt(32)
        val value1 = data1.toLong() or (data2.toLong() shl 32)

        input.seekTo(4)
        val value2 = input.readLong(64)

        Assertions.assertEquals(value2, value1)
    }
}