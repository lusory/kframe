import me.lusory.kframe.gradle.DependencyVersions
import me.lusory.kframe.gradle.addPublication
import me.lusory.kframe.gradle.enableTests

dependencies {
    api(group = "io.github.microutils", name = "kotlin-logging-jvm", version = DependencyVersions.KT_LOGGING)
    runtimeOnly(group = "org.slf4j", name = "slf4j-simple", version = DependencyVersions.SLF4J)
}

enableTests()
addPublication()