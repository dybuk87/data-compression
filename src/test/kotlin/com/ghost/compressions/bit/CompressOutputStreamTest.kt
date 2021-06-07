package com.ghost.compressions.bit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CompressOutputStreamTest {

    @Test
    fun test1() {
        Assertions.assertEquals(1, bitMask(1))
        Assertions.assertEquals(255, bitMask(8))
        Assertions.assertEquals(7, bitMask(3))
    }

    @Test
    fun test2() {
        val out = CompressOutputStream()

        out.insert(0b11, 2)
        out.insert(0b1011011010011100011, 19)
        out.insert(0b10101010, 8)

        Assertions.assertEquals("10001111", out.buffer[0].toUByte().toString(2).padStart(8, '0'))
        Assertions.assertEquals("11010011", out.buffer[1].toUByte().toString(2).padStart(8, '0'))
        Assertions.assertEquals("01010110", out.buffer[2].toUByte().toString(2).padStart(8, '0'))
        Assertions.assertEquals("00010101", out.buffer[3].toUByte().toString(2).padStart(8, '0'))
    }

    @Test
    fun test3() {
        val out = CompressOutputStream()
        out.insert(0x1FADDF0975274532, 64)

        Assertions.assertEquals(0x32, out.buffer[0])
        Assertions.assertEquals(0x45, out.buffer[1])
        Assertions.assertEquals(0x27, out.buffer[2])
        Assertions.assertEquals(0x75, out.buffer[3])
        Assertions.assertEquals(0x09, out.buffer[4])
        Assertions.assertEquals(0xDF, out.buffer[5].toInt() and 0xff)
        Assertions.assertEquals(0xAD, out.buffer[6].toInt() and 0xff)
        Assertions.assertEquals(0x1F, out.buffer[7])
    }


    @Test
    fun test4() {
        val out = CompressOutputStream()

        out.insert(252, 12)

        out.align8()

        Assertions.assertEquals(16, out.position)
    }

    @Test
    fun test5() {
        val out = CompressOutputStream()

        out.insert(252, 12)

        out.align16()

        Assertions.assertEquals(16, out.position)
    }

    @Test
    fun test6() {
        val out = CompressOutputStream()

        out.insert(252, 4)

        out.align32()

        Assertions.assertEquals(32, out.position)
    }


}