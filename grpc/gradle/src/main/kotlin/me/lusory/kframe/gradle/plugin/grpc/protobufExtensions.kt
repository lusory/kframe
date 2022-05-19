package me.lusory.kframe.gradle.plugin.grpc

import com.google.protobuf.gradle.*
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GradleVersion

fun Project.configureGrpc() {
    pluginManager.apply(ProtobufPlugin::class.java)

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${DependencyVersions.PROTOC}"
        }

        plugins {
            id("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:${DependencyVersions.GRPC}"
            }
            id("grpckt") {
                artifact = "io.grpc:protoc-gen-grpc-kotlin:${DependencyVersions.GRPC_KOTLIN}"
            }
        }

        generateProtoTasks {
            all().forEach {
                it.plugins {
                    id("grpc")
                    id("grpckt")
                }
                it.builtins {
                    id("kotlin")
                }
            }
        }

        generatedFilesBaseDir = "$buildDir/generatedProto"
    }

    @Suppress("DEPRECATION")
    fun getSourceSets(): SourceSetContainer =
        if (GradleVersion.version(gradle.gradleVersion) < GradleVersion.version("7.1")) convention.getPlugin(JavaPluginConvention::class.java).sourceSets
        else extensions.getByType(JavaPluginExtension::class.java).sourceSets

    getSourceSets().forEach { it.java.srcDirs("$buildDir/generatedProto/${it.name}/java", "$buildDir/generatedProto/${it.name}/kotlin") }
}