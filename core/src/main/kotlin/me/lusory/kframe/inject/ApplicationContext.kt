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

package me.lusory.kframe.inject

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface ApplicationContext {
    val components: Set<Any>

    operator fun get(klass: KClass<*>): Any? = components.firstOrNull { it::class.isSubclassOf(klass) }

    operator fun invoke(block: Builder.() -> Unit): ApplicationContext = ApplicationContextImpl.Builder().also(block).build()

    interface Builder {
        fun <T : Any> newComponent(block: () -> T): T

        fun <T : Any> newComponentProvider(block: () -> T): () -> T = { newComponent(block) }

        fun build(): ApplicationContext
    }
}