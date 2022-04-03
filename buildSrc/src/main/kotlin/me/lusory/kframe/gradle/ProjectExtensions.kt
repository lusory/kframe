package me.lusory.kframe.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun Project.addPublication() {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
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