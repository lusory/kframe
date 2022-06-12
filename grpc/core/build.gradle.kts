import me.lusory.kframe.gradle.DependencyVersions

group = "me.lusory.kframe.net"

dependencies {
    api(group = "io.grpc", name = "grpc-kotlin-stub", version = DependencyVersions.GRPC_KOTLIN) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")

        constraints {
            api(group = "io.grpc", name = "grpc-stub", version = DependencyVersions.GRPC)
        }
    }
    api(group = "io.grpc", name = "grpc-protobuf", version = DependencyVersions.GRPC)
    api(group = "com.google.protobuf", name = "protobuf-kotlin", version = DependencyVersions.PROTOBUF)
    compileOnlyApi(group = "org.apache.tomcat", name = "tomcat-annotations-api", version = DependencyVersions.TOMCAT_ANNOTATIONS)
}