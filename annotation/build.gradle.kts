import me.lusory.kframe.gradle.DependencyVersions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(group = "com.google.devtools.ksp", name = "symbol-processing-api", version = DependencyVersions.KSP)
    implementation(group = "com.squareup", name = "kotlinpoet", version = DependencyVersions.KOTLINPOET)
    implementation(group = "com.squareup", name = "kotlinpoet-ksp", version = DependencyVersions.KOTLINPOET)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}