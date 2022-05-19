import me.lusory.kframe.gradle.DependencyVersions

group = "me.lusory.kframe.net"

dependencies {
    api(group = "io.grpc", name = "grpc-stub", version = DependencyVersions.GRPC)
    api(group = "io.grpc", name = "grpc-protobuf", version = DependencyVersions.GRPC)
    compileOnlyApi(group = "org.apache.tomcat", name = "tomcat-annotations-api", version = DependencyVersions.TOMCAT_ANNOTATIONS)
}