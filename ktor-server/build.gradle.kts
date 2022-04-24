import me.lusory.kframe.gradle.DependencyVersions

dependencies {
    api(group = "io.ktor", name = "ktor-server-core", version = DependencyVersions.KTOR) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
}