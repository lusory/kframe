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

import me.lusory.kframe.util.InternalAPI
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * A data-holding interface with all component instances currently known.
 *
 * @author zlataovce
 * @since 0.0.1
 */
interface ApplicationContext {
    /**
     * A [Set] of component instances.
     */
    val components: Set<Any>

    /**
     * A convenience operator for fetching a component instance by its class/superclass.
     *
     * @param klass the class
     * @return the component instance, null if not found
     */
    operator fun get(klass: KClass<*>): Any? = components.firstOrNull { it::class.isSubclassOf(klass) }

    /**
     * A DSL builder for [ApplicationContext].
     *
     * @author zlataovce
     * @since 0.0.1
     */
    @InternalAPI
    interface Builder {
        /**
         * Appends a new component instance to the builder.
         *
         * @param block the component supplier
         * @return the component instance
         */
        fun <T : Any> newComponent(block: () -> T): T

        /**
         * Wraps the provided component supplier and appends any provided instance to the builder.
         *
         * @param block the component supplier
         * @return the wrapped supplier
         */
        fun <T : Any> newComponentProvider(block: () -> T): () -> T = { newComponent(block) }

        /**
         * Registers a hook to run after the context has been built.
         *
         * @param block the hook
         */
        fun afterBuild(block: (ApplicationContext) -> Unit)

        /**
         * Builds the [ApplicationContext].
         *
         * @return the application context
         */
        fun build(): ApplicationContext
    }
}

/**
 * Creates an [ApplicationContext] instance with a builder.
 *
 * @param block the builder mutator
 * @return the application context
 */
@InternalAPI // use dependency injection to get a context instance
fun applicationContext(block: ApplicationContext.Builder.() -> Unit): ApplicationContext = ApplicationContextImpl.Builder().also(block).build()