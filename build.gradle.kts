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
            "org.jetbrains.dokka.base.DokkaBase" to """{
                "footerMessage": "© ${Year.now().value} Copyright lusory contributors",
                "customStyleSheets": ["${rootProject.file("assets/style-overrides.css").absolutePath}"],
                "customAssets": ["${rootProject.file("assets/logo-icon.svg").absolutePath}"]
            }""".trimIndent()
        ))

        pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
            version = project.version as String
            if (this@withType is DokkaMultiModuleTask) {
                olderVersionsDir = rootProject.file("build/dokka/versioned").also { it.mkdirs() }

                rootProject.file("build/dokka/versioned/index.html").writeText(
                    """
                        <!DOCTYPE html>
                        <html>
                        <head>
                          <meta charset="utf-8">
                          <title>Redirecting</title>
                          <noscript>
                            <meta http-equiv="refresh" content="1; url=latest/" />
                          </noscript>
                          <script>
                            window.location.replace("$version/" + window.location.hash);
                          </script>
                        </head>
                        <body>
                          Redirecting to <a href="$version/">$version/</a>...
                        </body>
                        </html>
                    """.trimIndent()
                )

                outputDirectory.set(rootProject.file("build/dokka/versioned/$version").also { if (it.isDirectory) it.deleteRecursively() })
            }
        }
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