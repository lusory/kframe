import me.lusory.kframe.gradle.DependencyVersions

group = "me.lusory.kframe.data"

dependencies {
    api(project(":core"))
    api(group = "org.ktorm", name = "ktorm-core", version = DependencyVersions.KTORM) {
        exclude(group = "org.jetbrains.kotlin")
    }
    api(group = "com.zaxxer", name = "HikariCP", version = DependencyVersions.HIKARI)
    runtimeOnly(group = "org.tinylog", name = "slf4j-tinylog", version = DependencyVersions.TINYLOG)
}