import me.lusory.kframe.gradle.DependencyVersions

plugins {
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

group = "me.lusory.kframe.net"

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    implementation(group = "gradle.plugin.com.google.protobuf", name = "protobuf-gradle-plugin", version = DependencyVersions.PROTOBUF_GRADLE)
}

buildConfig {
    forClass(packageName = "me.lusory.kframe.gradle.plugin.grpc", className = "DependencyVersions") {
        buildConfigField("String", "PROTOC", "\"${DependencyVersions.PROTOC}\"")
        buildConfigField("String", "GRPC", "\"${DependencyVersions.GRPC}\"")
        buildConfigField("String", "GRPC_KOTLIN", "\"${DependencyVersions.GRPC_KOTLIN}\"")
    }
}