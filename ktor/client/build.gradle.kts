import me.lusory.kframe.gradle.DependencyVersions

group = "me.lusory.kframe.net"

dependencies {
    api(project(":core"))
    api(group = "io.ktor", name = "ktor-client-core-jvm", version = DependencyVersions.KTOR) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
    }
}