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

package me.lusory.kframe.test.inject

import me.lusory.kframe.inject.ApplicationContext
import me.lusory.kframe.inject.Autowired
import me.lusory.kframe.inject.applicationContext
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

class ApplicationContextTest {
    @Test
    fun buildContext() {
        val expected: Set<Any> = setOf(object {}, object {})

        assertContentEquals(
            expected,
            actual = applicationContext { for (obj in expected) { newComponent { obj } } }.components.asIterable()
        )

        assertContentEquals(
            expected,
            actual = applicationContext { for (obj in expected) { newComponentProvider { obj }() } }.components.asIterable()
        )
    }

    @Test
    fun injectProperties() {
        applicationContext {
            val var0: TestObj1 = newComponent {
                TestObj1()
            }
            newComponent {
                "test1"
            }
            newComponent {
                "test2"
            }
            afterBuild { context ->
                println(var0)
                assertNotNull(var0.context)
                assertNotNull(var0.strings)
                assertContentEquals(expected = listOf("test1", "test2"), actual = var0.strings)
            }
        }
    }

    class TestObj1 {
        lateinit var context: ApplicationContext
        @Autowired
        lateinit var strings: Collection<String>

        override fun toString(): String {
            return "TestObj1(context=$context, strings=$strings)"
        }
    }
}