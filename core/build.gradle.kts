@file:Suppress("RemoveRedundantQualifierName")

import me.lusory.kframe.gradle.DependencyVersions
import me.lusory.kframe.gradle.addPublication
import me.lusory.kframe.gradle.enableTests

plugins {
    id("com.google.devtools.ksp") version me.lusory.kframe.gradle.DependencyVersions.KSP
}

dependencies {
    api(group = "io.github.microutils", name = "kotlin-logging-jvm", version = DependencyVersions.KT_LOGGING)
    runtimeOnly(group = "org.slf4j", name = "slf4j-simple", version = DependencyVersions.SLF4J)

    ksp(project(":library-annotation"))
}

enableTests()
addPublication()