import me.lusory.kframe.gradle.DependencyVersions

plugins {
    id("com.gradle.plugin-publish") version "0.21.0"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    `java-gradle-plugin`
}

dependencies {
    implementation(group = "com.google.devtools.ksp", name = "symbol-processing-gradle-plugin", version = DependencyVersions.KSP)
}

gradlePlugin {
    plugins {
        create("kframeLibrary") {
            id = "me.lusory.kframe.library"
            displayName = "Plugin for setting up KFrame in a library"
            description = "A plugin for setting up KFrame in a library automatically"
            implementationClass = "me.lusory.kframe.gradle.plugin.library.KFrameLibraryPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/lusory/kframe"
    vcsUrl = "https://github.com/lusory/kframe.git"
    tags = listOf("kframe", "autoconfiguration", "library")
}

buildConfig {
    forClass(packageName = "me.lusory.kframe.gradle", className = "BuildInfo") {
        buildConfigField("String", "VERSION", "\"${version as String}\"")
    }
}