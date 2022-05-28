import me.lusory.kframe.gradle.DependencyVersions
import me.lusory.kframe.gradle.enableTests

group = "me.lusory.kframe.data"

dependencies {
    api(project(":core"))
    api(group = "org.ktorm", name = "ktorm-core", version = DependencyVersions.KTORM) {
        exclude(group = "org.jetbrains.kotlin")
    }
    api(group = "com.zaxxer", name = "HikariCP", version = DependencyVersions.HIKARI)

    testImplementation(group = "com.h2database", name = "h2", version = DependencyVersions.H2)
}

enableTests()

java.disableAutoTargetJvm() // disable Java-bound dependency targeting (due to Hikari)