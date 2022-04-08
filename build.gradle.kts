import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import java.time.Year

buildscript {
    dependencies {
        classpath(group = "org.jetbrains.dokka", name = "versioning-plugin", version = me.lusory.kframe.gradle.DependencyVersions.DOKKA)
    }
}

plugins {
    kotlin("jvm") version me.lusory.kframe.gradle.DependencyVersions.KOTLIN apply false
    id("org.cadixdev.licenser") version "0.6.1"
    id("org.jetbrains.dokka") version me.lusory.kframe.gradle.DependencyVersions.DOKKA
    `maven-publish`
    `java-library`
}

repositories {
    mavenCentral() // dokka
}

allprojects {
    group = "me.lusory.kframe"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.cadixdev.licenser")
        plugin("org.jetbrains.dokka")
    }

    repositories {
        mavenCentral()
        maven("https://repo.lusory.dev/releases")
        maven("https://repo.lusory.dev/snapshots")
    }

    dependencies {
        // added by user
        compileOnly(kotlin("stdlib"))
        compileOnly(kotlin("reflect"))
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = if ((project.version as String).endsWith("-SNAPSHOT")) uri("https://repo.lusory.dev/snapshots")
                    else uri("https://repo.lusory.dev/releases")
                credentials {
                    username = System.getenv("REPO_USERNAME")
                    password = System.getenv("REPO_PASSWORD")
                }
            }
        }
    }

    configurations.all {
        resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
    }

    license {
        header(rootProject.file("license_header.txt"))
    }

    applyDokka()
}

applyDokka()

fun Project.applyDokka() {
    dependencies {
        dokkaPlugin(group = "org.jetbrains.dokka", name = "versioning-plugin", version = me.lusory.kframe.gradle.DependencyVersions.DOKKA)
    }

    tasks.withType<AbstractDokkaTask> {
        pluginsMapConfiguration.set(mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """{ "footerMessage": "© ${Year.now().value} Copyright lusory contributors" }"""
        ))

        pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
            version = project.version as String
            if (this@withType is DokkaMultiModuleTask) {
                olderVersionsDir = rootProject.file("build/dokka/versioned")
            }
        }

        outputDirectory.set(rootProject.file("build/dokka/versioned/${version as String}").also { if (it.isDirectory) it.deleteRecursively() })
    }

    if (rootProject != this@applyDokka) {
        tasks.withType<AbstractDokkaLeafTask> {
            dokkaSourceSets.configureEach {
                includes.fromIfExists(this@applyDokka, "src/dokka-symbols.md")
            }
        }
    }
}

fun ConfigurableFileCollection.fromIfExists(project: Project, vararg paths: Any) {
    if (paths.all { project.file(it).isFile }) {
        from(paths)
    }
}