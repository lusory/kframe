package me.lusory.kframe.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

fun Project.addPublication() {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks["kotlinSourcesJar"])
                pom {
                    name.set("kframe")
                    description.set("A lightweight application framework for Kotlin")
                    url.set("https://github.com/lusory/kframe")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://github.com/lusory/kframe/blob/master/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("zlataovce")
                            name.set("Matouš Kučera")
                            email.set("mk@kcra.me")
                        }
                        developer {
                            id.set("tlkh40") // troll
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/lusory/kframe.git")
                        developerConnection.set("scm:git:ssh://github.com/lusory/kframe.git")
                        url.set("https://github.com/lusory/kframe/tree/master")
                    }
                }
            }
        }
    }
}

fun Project.enableTests() {
    dependencies {
        add("testImplementation", kotlin("reflect"))

        add("testImplementation", kotlin("test"))
        add("testImplementation", "org.mockito.kotlin:mockito-kotlin:${DependencyVersions.MOCKITO_KT}") {
            constraints {
                // bump mockito version
                add("testImplementation", "org.mockito:mockito-core:${DependencyVersions.MOCKITO}")
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

private val DOKKA_PROJECTS: MutableMap<Project, Boolean> = mutableMapOf()

var Project.applyDokka: Boolean
    get() = DOKKA_PROJECTS[this] ?: true
    set(value) {
        DOKKA_PROJECTS[this] = value
    }

private val PUBLISH_PROJECTS: MutableMap<Project, Boolean> = mutableMapOf()

var Project.publish: Boolean
    get() = PUBLISH_PROJECTS[this] ?: true
    set(value) {
        PUBLISH_PROJECTS[this] = value
    }
