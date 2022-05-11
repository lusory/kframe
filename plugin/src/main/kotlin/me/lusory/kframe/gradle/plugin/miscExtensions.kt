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

package me.lusory.kframe.gradle.plugin

import org.gradle.api.artifacts.dsl.DependencyHandler
import java.util.*

/**
 * Clears the collection if all items are logically empty (empty string).
 *
 * @since 0.0.1
 */
fun MutableCollection<String>.clearIfLogicallyEmpty() = clearIf { it.isEmpty() }

/**
 * Clears the collection if all items match the supplied predicate.
 *
 * @param predicate the predicate
 * @since 0.0.1
 */
inline fun <T> MutableCollection<T>.clearIf(predicate: (T) -> Boolean) {
    if (all(predicate)) {
        clear()
    }
}

/**
 * Encodes this string to Base64.
 *
 * @return the encoded string
 * @since 0.0.1
 */
fun String.toBase64(): String = Base64.getEncoder().encodeToString(encodeToByteArray())