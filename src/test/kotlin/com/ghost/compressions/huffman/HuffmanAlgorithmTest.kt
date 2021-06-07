package com.ghost.compressions.huffman

import com.ghost.compressions.huffman.data.HuffmanItem
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

// help navigate in tree in tests
fun HuffmanItem?.fLeft() = (this as HuffmanItem.Node).left!!
fun HuffmanItem?.fRight() = (this as HuffmanItem.Node).right!!
fun HuffmanItem?.ch() = (this as HuffmanItem.Leaf).char!!

class HuffmanAlgorithmTest {

    private val stats = mapOf<Byte, Long>(
        Pair(1, 5),
        Pair(2, 3),
        Pair(3, 8),
        Pair(4, 2),
        Pair(5, 1)
    )

    @Test
    fun testStats() {
        val data = byteArrayOf(3, 3, 5, 3, 1, 3, 4, 5)

        val stat = collectTextStats(data)

        Assertions.assertEquals(4, stat.size)
        Assertions.assertEquals(1, stat[1])
        Assertions.assertEquals(4, stat[3])
        Assertions.assertEquals(1, stat[4])
        Assertions.assertEquals(2, stat[5])
    }

    @Test
    fun testBuildNodesLeafOnly() {
        val nodes = buildNodes(stats)

        Assertions.assertEquals(stats.size, nodes.size)
        for (node in nodes) {
            Assertions.assertTrue(node is HuffmanItem.Leaf)
        }
    }

    @Test
    fun testBuildNodesProbability() {
        val nodes = buildNodes(stats)
            .sortedBy { it.probability }

        val total = stats.values.sum()

        val expected = mapOf<Byte, Double>(
            Pair(1, 5.0 / total),
            Pair(2, 3.0 / total),
            Pair(3, 8.0 / total),
            Pair(4, 2.0 / total),
            Pair(5, 1.0 / total),
        )

        for (node in nodes) {
            val leaf = node as HuffmanItem.Leaf
            Assertions.assertEquals(expected[leaf.char], leaf.probability)
        }
    }

    @Test
    fun findInsertPositionTest() {
        val list = listOf(
            HuffmanItem.Leaf(4, 0.07),
            HuffmanItem.Leaf(5, 0.09),
            HuffmanItem.Leaf(6, 0.16),
            HuffmanItem.Leaf(7, 0.20),
        )

        Assertions.assertEquals(3, findInsertPosition(HuffmanItem.Leaf(1, 0.19), list))
    }

    @Test
    fun testBuildHuffmanTree() {
        val total = 1 + 2 + 8 + 3 + 5

        val list = listOf(
            HuffmanItem.Leaf(5, 1.0 / total),
            HuffmanItem.Leaf(4, 2.0 / total),
            HuffmanItem.Leaf(2, 3.0 / total),
            HuffmanItem.Leaf(1, 5.0 / total),
            HuffmanItem.Leaf(3, 8.0 / total)
        )

        val root = buildHuffmanTree(list) as HuffmanItem.Node

        Assertions.assertEquals(1.0, root.probability)

        Assertions.assertEquals(11.0 / total, root.fRight().probability)
        Assertions.assertEquals(8.0 / total, root.fLeft().probability)
        Assertions.assertEquals(3, root.fLeft().ch())

        Assertions.assertEquals(6.0 / total, root.fRight().fRight().probability)
        Assertions.assertEquals(5.0 / total, root.fRight().fLeft().probability)
        Assertions.assertEquals(1, root.fRight().fLeft().ch())

        Assertions.assertEquals(3.0 / total, root.fRight().fRight().fRight().probability)
        Assertions.assertEquals(3.0 / total, root.fRight().fRight().fLeft().probability)
        Assertions.assertEquals(2, root.fRight().fRight().fLeft().ch())

        Assertions.assertEquals(2.0 / total, root.fRight().fRight().fRight().fRight().probability)
        Assertions.assertEquals(1.0 / total, root.fRight().fRight().fRight().fLeft().probability)

        Assertions.assertEquals(4, root.fRight().fRight().fRight().fRight().ch())
        Assertions.assertEquals(5, root.fRight().fRight().fRight().fLeft().ch())
    }

    @Test
    fun buildHuffmanCodeTest() {
        val node1 = HuffmanItem.Node(
            0.2,
            HuffmanItem.Leaf(4, 0.1),
            HuffmanItem.Leaf(5, 0.1)
        )

        val node2 = HuffmanItem.Node(
            0.6,
            node1,
            HuffmanItem.Leaf(3, 0.4)
        )

        val node3 = HuffmanItem.Node(
            0.4,
            HuffmanItem.Leaf(2, 0.3),
            HuffmanItem.Leaf(1, 0.1)
        )

        val root = HuffmanItem.Node(
            1.0,
            node2,
            node3
        )

        val codes = buildHuffmanCodes(root)


        Assertions.assertEquals(5, codes.size)
        val c1 = codes.find { it.char == 1.toByte() }!!
        val c2 = codes.find { it.char == 2.toByte() }!!
        val c3 = codes.find { it.char == 3.toByte() }!!
        val c4 = codes.find { it.char == 4.toByte() }!!
        val c5 = codes.find { it.char == 5.toByte() }!!

        Assertions.assertEquals(2, c1.bitLength)
        Assertions.assertEquals(2, c2.bitLength)
        Assertions.assertEquals(2, c3.bitLength)
        Assertions.assertEquals(3, c4.bitLength)
        Assertions.assertEquals(3, c5.bitLength)

        Assertions.assertEquals(3, c1.encode)
        Assertions.assertEquals(1, c2.encode)
        Assertions.assertEquals(2, c3.encode)
        Assertions.assertEquals(0, c4.encode)
        Assertions.assertEquals(4, c5.encode)
    }

}