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

/**
 * Marks a top-level or a class function to run when the specified action is ran.
 *
 * @param action the action
 *
 * @since 0.0.1
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class On(val action: Action)

/**
 * Actions for the [On] annotation.
 */
enum class Action {
    /**
     * The [ApplicationContext] was created.
     *
     * @since 0.0.1
     */
    CONTEXT_CREATE,

    /**
     * The JVM received an interrupt/quit signal.
     *
     * @since 0.0.1
     */
    SHUTDOWN
}
