import me.lusory.kframe.gradle.DependencyVersions
import me.lusory.kframe.gradle.addPublication

dependencies {
    implementation(group = "com.google.devtools.ksp", name = "symbol-processing-api", version = DependencyVersions.KSP)
}

addPublication()