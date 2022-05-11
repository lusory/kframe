import me.lusory.kframe.gradle.DependencyVersions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api(group = "com.google.devtools.ksp", name = "symbol-processing-api", version = DependencyVersions.KSP)
    api(group = "com.squareup", name = "kotlinpoet", version = DependencyVersions.KOTLINPOET)
    api(group = "com.squareup", name = "kotlinpoet-ksp", version = DependencyVersions.KOTLINPOET)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}