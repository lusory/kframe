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

package me.lusory.kframe.gradle.plugin.library

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import me.lusory.kframe.gradle.BuildInfo
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The KFrame library Gradle plugin main class, instantiated via the Java Service Loader API.
 *
 * @since 0.0.1
 */
class KFrameLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(KspGradleSubplugin::class.java)

        target.dependencies.add("ksp", "me.lusory.kframe:annotation-library:${BuildInfo.VERSION}")
    }
}