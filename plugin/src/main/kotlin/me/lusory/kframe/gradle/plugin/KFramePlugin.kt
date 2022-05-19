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

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import me.lusory.kframe.gradle.BuildInfo
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaApplication
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.util.*
import java.util.zip.ZipFile

/**
 * The KFrame Gradle plugin main class, instantiated via the Java Service Loader API.
 *
 * @since 0.0.1
 */
class KFramePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        try {
            target.pluginManager.apply(KotlinPlatformJvmPlugin::class.java)
        } catch (ignored: NoClassDefFoundError) {
            throw GradleException("The Kotlin Gradle plugin needs to be available on the classpath for KFrame to work")
        }

        val extension: KFramePluginExtension = target.extensions.create("kframe", KFramePluginExtension::class.java)

        // apply ksp and add generated source set
        target.pluginManager.apply(KspGradleSubplugin::class.java)
        target.extensions.configure(KotlinJvmProjectExtension::class.java) {
            it.sourceSets.forEach { sourceSet ->
                sourceSet.kotlin.srcDir("build/generated/ksp/${sourceSet.name}/kotlin")
            }
        }

        // add annotation module dependency, must not be in afterEvaluate else kspKotlin task doesn't get created
        target.dependencies.add("ksp", "me.lusory.kframe:annotation:${BuildInfo.VERSION}")

        // add 'kfrProcessor' alias for 'ksp'
        target.configurations.create("kfrProcessor") {
            it.extendsFrom(target.configurations.getByName("ksp"))
        }

        // afterEvaluate is needed to load the extension properly
        target.afterEvaluate {
            if (extension.isApplication) {
                // apply shadowJar, add dependency injection metadata and set main class name in jar manifest
                target.pluginManager.apply(ShadowPlugin::class.java)

                target.extensions.configure(KspExtension::class.java) { ext ->
                    ext.arg("kframe.dependencyInjection.packageName", extension.mainPackageName)
                    ext.arg("kframe.dependencyInjection.className", extension.mainClassName)
                }

                target.tasks.withType(Jar::class.java) { jar ->
                    jar.manifest { manifest ->
                        manifest.attributes["Main-Class"] = extension.mainFQClassName
                    }
                }

                target.pluginManager.apply(ApplicationPlugin::class.java)
                target.extensions.configure(JavaApplication::class.java) { ext ->
                    ext.mainClass.set(extension.mainFQClassName)
                }
            }

            if (extension.applyKotlin && extension.isApplication) {
                // apply kotlin-stdlib and reflect by the kotlin plugin version
                val kotlinVersion: String = target.getKotlinPluginVersion()
                target.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                target.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            }

            // add core module dependency
            target.dependencies.add(if (extension.isApplication) "implementation" else "compileOnly", "me.lusory.kframe:core:${BuildInfo.VERSION}")

            // add dependency injection metadata from dependencies
            // TODO: replace with https://github.com/google/ksp/issues/431
            val members: MutableSet<String> = mutableSetOf()
            val classes: MutableSet<String> = mutableSetOf()
            val inits: MutableSet<String> = mutableSetOf()
            target.configurations.getByName("compileClasspath").resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                ZipFile(artifact.file).use { zipFile ->
                    zipFile.entries().iterator().forEach { entry ->
                        if (entry.name.substringAfterLast('/') == "inject.properties") {
                            val props: Properties = Properties().also { it.load(zipFile.getInputStream(entry)) }
                            members.addAll((props["kframe.dependencyInjection.members"] as? String ?: "").split(',').toMutableList().apply { clearIfLogicallyEmpty() })
                            classes.addAll((props["kframe.dependencyInjection.classes"] as? String ?: "").split(',').toMutableList().apply { clearIfLogicallyEmpty() })
                            inits.addAll((props["kframe.dependencyInjection.inits"] as? String ?: "").split(',').toMutableList().apply { clearIfLogicallyEmpty() })
                        }
                    }
                }
            }

            target.extensions.configure(KspExtension::class.java) { ext ->
                // https://github.com/google/ksp/issues/154
                ext.arg("kframe.dependencyInjection.members", members.joinToString(",").toBase64())
                ext.arg("kframe.dependencyInjection.classes", classes.joinToString(",").toBase64())
                ext.arg("kframe.dependencyInjection.inits", inits.joinToString(",").toBase64())
            }

            // enable dependency injection if application
            // else enable inject.properties generation
            target.extensions.configure(KspExtension::class.java) { ext ->
                ext.arg("kframe.${if (extension.isApplication) "dependencyInjection" else "injectProperties"}.enabled", "true")
            }
        }
    }
}