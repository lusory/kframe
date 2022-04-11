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
 * Marks a top-level function or a class method to run after the specified action has been performed.
 *
 * @since 0.0.1
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Listen(val action: Action)

/**
 * An action that can be triggered within the application lifecycle.
 *
 * @since 0.0.1
 */
enum class Action {
    /**
     * The application context was populated.
     */
    INIT,

    /**
     * The JVM received a `SIGINT`.
     */
    CLOSE
}
