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

/**
 * Marks a top-level or a class function to run when the [ApplicationContext] is built.
 *
 * Handler invocation order is specified by the [priority] parameter, the higher it is, the earlier it is invoked.
 *
 * @param priority the initialization priority, can be custom or from [InitPriority]
 *
 * @since 0.0.1
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Init(val priority: Int = InitPriority.NORMAL)

/**
 * Predefined initialization priorities for [Init].
 */
object InitPriority {
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
     * Very low priority, intended for blocking the main thread (e.g. starting a web server, server socket).
     */
    const val BLOCKING = -20

    /**
     * Internal initialization priority, used for registering internal shutdown hooks etc.
     */
    @InternalAPI
    const val INTERNAL_LOW = -10

    /**
     * Internal initialization priority, used for initializing loggers etc.
     */
    @InternalAPI
    const val INTERNAL_HIGH = 10
}
