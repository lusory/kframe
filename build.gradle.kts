plugins {
    kotlin("jvm") version me.lusory.kframe.gradle.DependencyVersions.KOTLIN apply false
    id("org.cadixdev.licenser") version "0.6.1"
    `maven-publish`
    `java-library`
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
    }

    repositories {
        mavenCentral()
        maven("https://repo.lusory.dev/releases")
        maven("https://repo.lusory.dev/snapshots")
    }

    dependencies {
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