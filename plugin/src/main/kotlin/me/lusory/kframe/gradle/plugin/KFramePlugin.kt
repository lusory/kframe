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

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import me.lusory.kframe.gradle.BuildInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.util.*
import java.util.zip.ZipFile

/**
 * The KFrame Gradle plugin main class, instantiated via the Java Service Loader API.
 *
 * @author zlataovce
 * @since 0.0.1
 */
class KFramePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension: KFramePluginExtension = target.extensions.create("kframe", KFramePluginExtension::class.java)

        target.pluginManager.apply(KspGradleSubplugin::class.java)

        target.extensions.configure(KspExtension::class.java) { ext ->
            ext.arg("packageName", extension.mainPackageName)
            ext.arg("className", extension.mainClassName)
        }

        try {
            if (!target.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")) {
                target.pluginManager.apply(KotlinPlatformJvmPlugin::class.java)
            }

            target.extensions.configure(KotlinJvmProjectExtension::class.java) {
                it.sourceSets.forEach { sourceSet ->
                    sourceSet.kotlin.srcDir("build/generated/ksp/${sourceSet.name}/kotlin")
                }
            }

            if (extension.applyKotlin) {
                val kotlinVersion: String = target.getKotlinPluginVersion()
                target.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                target.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            }
        } catch (ignored: NoClassDefFoundError) {
            throw RuntimeException("The Kotlin Gradle plugin needs to be available on the classpath for KFrame to work!")
        }

        target.dependencies.add("implementation", "me.lusory.kframe:core:${BuildInfo.VERSION}")
        target.dependencies.add("ksp", "me.lusory.kframe:annotation:${BuildInfo.VERSION}")

        target.tasks.withType(Jar::class.java) { jar ->
            jar.manifest { manifest ->
                manifest.attributes["Main-Class"] = extension.mainFQClassName
            }
        }

        // TODO: replace with https://github.com/google/ksp/issues/431
        target.afterEvaluate {
            val members: MutableSet<String> = mutableSetOf()
            val classes: MutableSet<String> = mutableSetOf()
            target.configurations.getByName("compileClasspath").resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                ZipFile(artifact.file).use { zipFile ->
                    zipFile.entries().iterator().forEach { entry ->
                        if (entry.name.substringAfterLast('/') == "kframe.properties") {
                            val props: Properties = Properties().also { it.load(zipFile.getInputStream(entry)) }
                            members.addAll((props["inject.members"] as? String ?: "").split(','))
                            classes.addAll((props["inject.classes"] as? String ?: "").split(','))
                        }
                    }
                }
            }

            target.extensions.configure(KspExtension::class.java) { ext ->
                ext.arg("injectMembers", members.joinToString(","))
                ext.arg("injectClasses", classes.joinToString(","))
            }
        }
    }
}