import me.lusory.kframe.gradle.DependencyVersions

plugins {
    id("com.gradle.plugin-publish") version "0.21.0"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = DependencyVersions.KOTLIN) // added by user
    implementation(group = "com.google.devtools.ksp", name = "symbol-processing-gradle-plugin", version = DependencyVersions.KSP)
    implementation(group = "gradle.plugin.com.github.johnrengelman", name = "shadow", version = DependencyVersions.SHADOW)
}

gradlePlugin {
    plugins {
        create("kframe") {
            id = "me.lusory.kframe"
            displayName = "Plugin for setting up KFrame"
            description = "A plugin for setting up KFrame automatically"
            implementationClass = "me.lusory.kframe.gradle.plugin.KFramePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/lusory/kframe"
    vcsUrl = "https://github.com/lusory/kframe.git"
    tags = listOf("kframe", "autoconfiguration")
}

buildConfig {
    forClass(packageName = "me.lusory.kframe.gradle", className = "BuildInfo") {
        buildConfigField("String", "VERSION", "\"${version as String}\"")
    }
}