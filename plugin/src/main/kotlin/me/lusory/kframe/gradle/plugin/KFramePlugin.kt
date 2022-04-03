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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.util.GradleVersion

class KFramePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(JavaPlugin::class.java)) {
            target.plugins.apply(JavaPlugin::class.java)
        }
        target.pluginManager.apply("com.google.devtools.ksp")

        target.dependencies.add("implementation", "me.lusory.kframe:core:0.0.1-SNAPSHOT")
        target.dependencies.add("ksp", "me.lusory.kframe:annotation:0.0.1-SNAPSHOT")

        getSourceSets(target).forEach { sourceSet ->
            sourceSet.allSource.srcDir("build/generated/src/${sourceSet.name}/kotlin")
        }

        target.tasks.withType(Jar::class.java) { jar ->
            jar.manifest { manifest ->
                manifest.attributes["Main-Class"] = "kframe.Main"
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getSourceSets(project: Project): SourceSetContainer =
        if (GradleVersion.version(project.gradle.gradleVersion) < GradleVersion.version("7.1")) project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        else project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
}