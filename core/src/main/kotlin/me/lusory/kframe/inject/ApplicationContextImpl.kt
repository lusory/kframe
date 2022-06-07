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
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

internal class ApplicationContextImpl(override val components: MutableSet<Any>) : ApplicationContext {
    companion object {
        private val APPLICATION_CONTEXT: KType = ApplicationContext::class.createType()
    }

    init {
        components.forEach { prepareComponent(it) }
    }

    private fun prepareComponent(component: Any) {
        component::class.memberProperties.stream()
            .filter { it.isLateinit && it is KMutableProperty1 }
            .forEach {
                if (it.returnType.isSubtypeOf(APPLICATION_CONTEXT)) {
                    @Suppress("UNCHECKED_CAST")
                    (it as KMutableProperty1<Any, ApplicationContext>).set(component, this)
                } else if (it.hasAnnotation<Autowired>() && it.returnType.jvmErasure.java.isAssignableFrom(List::class.java)) {
                    val elementType: Class<*> = it.returnType.arguments[0].type!!.jvmErasure.java

                    @Suppress("UNCHECKED_CAST")
                    (it as KMutableProperty1<Any, List<Any>>).set(component, components.filterIsInstance(elementType))
                }
            }
    }

    internal class Builder : ApplicationContext.Builder {
        private val components: MutableSet<Any> = mutableSetOf()
        private var afterBuildHook: ((ApplicationContext) -> Unit)? = null

        override fun <T : Any> newComponent(block: () -> T): T = block().also { components.add(it) }
        override fun afterBuild(block: (ApplicationContext) -> Unit) {
            afterBuildHook = block
        }

        override fun build(): ApplicationContextImpl = ApplicationContextImpl(components.toMutableSet()).also { afterBuildHook?.invoke(it) }
    }
}