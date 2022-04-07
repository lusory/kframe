import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import java.time.Year

buildscript {
    dependencies {
        classpath(group = "org.jetbrains.dokka", name = "dokka-base", version = me.lusory.kframe.gradle.DependencyVersions.DOKKA)
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
}

tasks.withType<DokkaTask> {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "© ${Year.now().value} Copyright lusory contributors"
    }
}