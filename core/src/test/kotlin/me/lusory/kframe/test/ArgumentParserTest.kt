package me.lusory.kframe.test

import me.lusory.kframe.argumentParser
import me.lusory.kframe.exceptions.ArgumentParseException
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ArgumentParserTest {
    @Test
    fun parseSingle() {
        assertContentEquals(
            expected = listOf(Pair(null, "test")),
            actual = argumentParser(arrayOf("test")).args
        )

        assertThrows<ArgumentParseException> {
            argumentParser(arrayOf("test", "test"))
        }
    }

    @Test
    fun parseShort() {
        val expected: List<Pair<String?, String?>> = listOf(Pair("t", "test"))

        assertContentEquals(expected, actual = argumentParser(arrayOf("-t=test")).args)
        assertContentEquals(expected, actual = argumentParser(arrayOf("-t", "test")).args)
    }

    @Test
    fun parseLong() {
        val expected: List<Pair<String?, String?>> = listOf(Pair("test", "test"))

        assertContentEquals(expected, actual = argumentParser(arrayOf("--test=test")).args)
        assertContentEquals(expected, actual = argumentParser(arrayOf("--test", "test")).args)
    }
}