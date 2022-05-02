import me.lusory.kframe.gradle.DependencyVersions
import me.lusory.kframe.gradle.enableTests

group = "me.lusory.kframe.data"

dependencies {
    api(project(":core"))
    api(group = "org.ktorm", name = "ktorm-core", version = DependencyVersions.KTORM) {
        exclude(group = "org.jetbrains.kotlin")
    }
    api(group = "com.zaxxer", name = "HikariCP", version = DependencyVersions.HIKARI)
    runtimeOnly(group = "org.tinylog", name = "slf4j-tinylog", version = DependencyVersions.TINYLOG)

    testImplementation(group = "com.h2database", name = "h2", version = DependencyVersions.H2)
}

enableTests()