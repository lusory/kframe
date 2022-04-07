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

import me.lusory.kframe.inject.applicationContext
import kotlin.test.Test
import kotlin.test.assertContentEquals

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
}