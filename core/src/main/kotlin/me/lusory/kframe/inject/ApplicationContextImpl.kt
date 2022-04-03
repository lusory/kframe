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

internal class ApplicationContextImpl(override val components: MutableSet<Any>) : ApplicationContext {
    override fun components(klass: KClass<*>): List<Any> = components.filter { it::class.isSubclassOf(klass) }

    internal class Builder : ApplicationContext.Builder {
        private val components: MutableSet<Any> = mutableSetOf()

        override fun addInstance(instance: Any) {
            components.add(instance)
        }

        override fun build(): ApplicationContextImpl = ApplicationContextImpl(components.toMutableSet())
    }
}