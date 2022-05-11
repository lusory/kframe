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

/**
 * A configuration extension for the KFrame Gradle plugin.
 *
 * Example:
 * ```kt
 * kframe {
 *     // sets the main class name for generation and in the jar manifest
 *     mainFQClassName = "kframe.Main"
 *     // or
 *     mainPackageName = "kframe"
 *     mainClassName = "Main"
 *     // should kotlin-stdlib and kotlin-reflect be applied to the project automatically (implementation)?
 *     // only takes effect if mode is APPLICATION
 *     applyKotlin = true
 *
 *     // are you making a starter or an application?
 *     mode = AnnotationProcessorMode.APPLICATION
 * }
 * ```
 *
 * @since 0.0.1
 */
abstract class KFramePluginExtension {
    /**
     * The fully qualified class name of the generated main class (e.g. kframe.Main).
     *
     * Only used in application mode.
     */
    var mainFQClassName: String
        get() = "${mainPackageName}.${mainClassName}"
        set(value) {
            mainPackageName = value.substringBeforeLast('.')
            mainClassName = value.substringAfterLast('.')
        }

    /**
     * The package name of the generated main class (e.g. kframe).
     *
     * Only used in application mode.
     */
    var mainPackageName: String = "kframe"

    /**
     * The simple class name of the generated main class (e.g. Main).
     *
     * Only used in application mode.
     */
    var mainClassName: String = "Main"

    /**
     * Should kotlin-stdlib and kotlin-reflect be applied to the project automatically (implementation)?
     *
     * Only used in application mode.
     */
    var applyKotlin: Boolean = true

    /**
     * The annotation processor mode.
     */
    var mode: AnnotationProcessorMode = AnnotationProcessorMode.APPLICATION

    internal val isApplication: Boolean
        get() = mode == AnnotationProcessorMode.APPLICATION

    internal val isStarter: Boolean
        get() = mode == AnnotationProcessorMode.STARTER
}

/**
 * Modes for the annotation processor.
 */
enum class AnnotationProcessorMode {
    /**
     * Starter mode.
     */
    STARTER,

    /**
     * Application mode.
     */
    APPLICATION
}