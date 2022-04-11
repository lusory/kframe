import me.lusory.kframe.gradle.DependencyVersions

dependencies {
    api(project(":core"))
    api(group = "org.ktorm", name = "ktorm-core", version = DependencyVersions.KTORM) {
        exclude(group = "org.jetbrains.kotlin")
    }
}