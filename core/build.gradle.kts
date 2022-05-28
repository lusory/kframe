@file:Suppress("RemoveRedundantQualifierName")

import me.lusory.kframe.gradle.DependencyVersions
import me.lusory.kframe.gradle.enableTests

plugins {
    id("com.google.devtools.ksp") version me.lusory.kframe.gradle.DependencyVersions.KSP
}

dependencies {
    api(group = "org.tinylog", name = "tinylog-api-kotlin", version = DependencyVersions.TINYLOG) {
        exclude(group = "org.jetbrains.kotlin")
    }
    runtimeOnly(group = "org.tinylog", name = "tinylog-impl", version = DependencyVersions.TINYLOG)
    runtimeOnly(group = "org.tinylog", name = "slf4j-tinylog", version = DependencyVersions.TINYLOG)

    ksp(project(":annotation"))
}

ksp {
    arg("kframe.injectProperties.enabled", "true")
}

enableTests()