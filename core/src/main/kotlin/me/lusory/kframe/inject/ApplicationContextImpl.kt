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

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

internal class ApplicationContextImpl(override val components: MutableSet<Any>) : ApplicationContext {
    companion object {
        private val TYPE: KType = ApplicationContext::class.createType()
    }

    init {
        components.forEach { prepareComponent0(it) }
    }

    private fun prepareComponent0(component: Any) {
        component::class.memberProperties.stream()
            .filter { it.isLateinit && it is KMutableProperty1 && it.returnType == TYPE }
            .forEach {
                @Suppress("UNCHECKED_CAST")
                (it as KMutableProperty1<Any, ApplicationContext>).set(component, this)
            }
    }

    internal class Builder : ApplicationContext.Builder {
        private val components: MutableSet<Any> = mutableSetOf()

        override fun <T : Any> newComponent(block: () -> T): T = block().also { components.add(it) }

        override fun build(): ApplicationContextImpl = ApplicationContextImpl(components.toMutableSet())
    }
}