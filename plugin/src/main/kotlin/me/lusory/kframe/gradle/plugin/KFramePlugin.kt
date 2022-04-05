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

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import me.lusory.kframe.gradle.BuildInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class KFramePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(KspGradleSubplugin::class.java)

        target.dependencies.add("implementation", "me.lusory.kframe:core:${BuildInfo.VERSION}")
        target.dependencies.add("ksp", "me.lusory.kframe:annotation:${BuildInfo.VERSION}")

        try {
            target.extensions.configure(KotlinJvmProjectExtension::class.java) {
                it.sourceSets.forEach { sourceSet ->
                    sourceSet.kotlin.srcDir("build/generated/ksp/${sourceSet.name}/kotlin")
                }
            }
        } catch (ignored: NoClassDefFoundError) {
            throw RuntimeException("The Kotlin Gradle plugin needs to be available on the classpath for KFrame to work!")
        }

        target.tasks.withType(Jar::class.java) { jar ->
            jar.manifest { manifest ->
                manifest.attributes["Main-Class"] = "kframe.Main"
            }
        }
    }
}