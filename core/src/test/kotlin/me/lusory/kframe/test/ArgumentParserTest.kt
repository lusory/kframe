/*
 * This file is part of kframe, licensed under the Apache License, Version 2.0 (the "License").
 *
 * Copyright (c) 2022-present lusory contributors
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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