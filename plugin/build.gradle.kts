plugins {
    id("com.gradle.plugin-publish") version "0.20.0"
    id("java-gradle-plugin")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.6.10-1.0.4")
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