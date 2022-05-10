import me.lusory.kframe.gradle.DependencyVersions

group = "me.lusory.kframe.data"

dependencies {
    api(project(":ktorm-core"))
    api(group = "org.ktorm", name = "ktorm-support-mysql", version = DependencyVersions.KTORM) {
        exclude(group = "org.jetbrains.kotlin")
    }
}