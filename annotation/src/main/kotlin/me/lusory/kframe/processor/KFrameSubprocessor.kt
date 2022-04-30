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

package me.lusory.kframe.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.util.*

/**
 * A KFrame annotation subprocessor base, implementations are instantiated via the Java Service Loader API.
 *
 * @since 0.0.1
 */
interface KFrameSubprocessor {
    /**
     * The processor initialization priority, the higher it is, the earlier it is invoked.
     */
    val priority: Int

    /**
     * Runs the subprocessor, called by [KFrameProcessor].
     *
     * **Runs only once, unlike a KSP processor.**
     *
     * @param environment the processor environment
     * @param resolver the resolver
     * @since 0.0.1
     */
    fun process(environment: SymbolProcessorEnvironment, resolver: Resolver)

    /**
     * Checks if [process] should be invoked, used by [KFrameProcessor].
     *
     * @param environment the processor environment
     * @param resolver the resolver
     * @since 0.0.1
     */
    fun shouldRun(environment: SymbolProcessorEnvironment): Boolean =
        environment.options["kframe.${javaClass.simpleName.replaceFirst("Subprocessor", "", ignoreCase = true).replaceFirstChar { it.lowercase(Locale.ENGLISH) }}.enabled"].toBoolean()
}

/**
 * Predefined initialization priorities for
 */
object ProcessorPriority {
    /**
     * Low priority.
     */
    const val LOW = -1

    /**
     * Normal priority.
     */
    const val NORMAL = 0

    /**
     * High priority.
     */
    const val HIGH = 1

    /**
     * Internal initialization priority.
     */
    const val INTERNAL_LOW = -10

    /**
     * Internal initialization priority.
     */
    const val INTERNAL_HIGH = 10
}