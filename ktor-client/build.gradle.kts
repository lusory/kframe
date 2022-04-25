import me.lusory.kframe.gradle.DependencyVersions

dependencies {
    api(group = "io.ktor", name = "ktor-client-core-jvm", version = DependencyVersions.KTOR) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
    }
    runtimeOnly(group = "org.tinylog", name = "slf4j-tinylog", version = DependencyVersions.TINYLOG)
}