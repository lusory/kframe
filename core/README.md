# Package me.lusory.kframe

Root package of kframe, all modules are subpackages of this

# Package me.lusory.kframe.exceptions

kframe specific exceptions, a part of the core module

# Package me.lusory.kframe.inject

Dependency injection API, a part of the core module

# Package me.lusory.kframe.util

Miscellaneous utilities, a part of the core module

# Package me.lusory.kframe.util.log

Logging related utilities, a part of the core module

# Module core

An application base, carrying the essential classes

## Features
- Compile-time dependency injection
- Application events
- Argument parsing

### Dependency injection

The annotation processor from the `annotation` module generates a main class file, which instantiates any top-level functions and classes annotated with `@Component`, filling their parameters/constructors with other components as needed.

If a class has multiple constructors, one can be prioritized by annotating it with `@Autowired`.

You can also force retrieval of one specific component instance by annotating the constructor/function parameter with `@Exact`, supplying the component's `name` parameter to the annotation.

> Supplying a name to the `@Exact` annotation locks the annotation processor search scope, which will result in a `DependencyResolveException` being thrown, if a component with the name wasn't found.

By default, all components are singleton (= only one instance is shared throughout the entire application). You can override this with the `@NonSingleton` annotation, which will make the annotation processor create a new instance for every dependent.

All component instances created by the annotation processor are registered with an `ApplicationContext` instance, you can acquire this instance by declaring a `lateinit` property in a component class:

> The `ApplicationContext` instance is injected after its construction, which is after all components have been instantiated, i.e. it won't be available in component constructors/init blocks.  
The annotation processor does not account for generic type parameters, `Collection`-like types can only be accurately injected with an `@Exact` annotation.

```kt
\@Component(name = "testComponent1")
class TestComponent1 {
    private lateinit var context: ApplicationContext
    fun foo() {
        // use the context
        context[TestComponent2::class] // returns a TestComponent2 instance
        context.components // all component instances
    }
}
```

### Application events

The `@Init` annotation can mark a function to run after the `ApplicationContext` has been built.

The function can be a part of a component or a top-level function, and it can accept zero or one parameter of type `ApplicationContext`.

A `shutdownHook` lambda is provided for convenience, this is a shortcut for `Runtime#addShutdownHook`.

```kt
\@Init
fun contextCreated() = println("Context created!")

\@Component
class Component0 {
    \@Init
    fun setupCleanup(context: ApplicationContext) {
        shutdownHook {
            // cleanup logic
        }
    }
}
```

### Argument parsing

An `ArgumentParser` class is provided for convenience, which can parse long (e.g. `--kframe.stuff`) and short arguments (e.g. `-k`) with their values (e.g. `--kframe stuff` `-k=stuff`).

An instance can be acquired via dependency injection. The raw arguments (from the `main` function) can also be injected:

```kt
\@Component
fun testComponent1(@Exact(name = "args") args: Array<String>): TestComponent1 = ...

\@Component
fun testComponent1(argParser: ArgumentParser): TestComponent1 = ...
```

All parsed arguments and their values are available in the `args` property of `ArgumentParser` in the form of a list with option-value pairs. A convenience operator for fetching argument values by their name is also available:

```kt
// arguments: '--kframe.stuff test' or '-k test'
\@Component
fun testComponent1(argParser: ArgumentParser): TestComponent1 {
    println(argParser["kframe.stuff", "k"] ?: "not found") // prints test
    // ...
}
```

Check out [the tests](https://github.com/lusory/kframe/blob/master/core/src/test/kotlin/me/lusory/kframe/test/ArgumentParserTest.kt) for more in-depth usages.

## Getting started

The basic requirement is the `core` module with the annotation processor from the `annotation` module.

### Gradle

#### Using the plugin

There is a Gradle plugin provided for convenience, you can be up and running in no time.

##### Kotlin DSL

```kotlin
import me.lusory.kframe.gradle.plugin.AnnotationProcessorMode

plugins {
    id("me.lusory.kframe") version "LATEST_VERSION_HERE"
    // make sure to also have the kotlin gradle plugin applied
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenCentral()
    maven("https://repo.lusory.dev/releases")
    maven("https://repo.lusory.dev/snapshots")
}

// optional

kframe {
    // sets the main class name for generation and in the jar manifest
    mainFQClassName = "kframe.Main"
    // or
    mainPackageName = "kframe"
    mainClassName = "Main"

    // should kotlin-stdlib and kotlin-reflect be applied to the project automatically (implementation)?
    // only takes effect if mode is APPLICATION
    applyKotlin = true

    // are you making a starter or an application?
    mode = AnnotationProcessorMode.APPLICATION
}

dependencies {
    // you can add a subprocessor here
    // kfrProcessor("me.lusory.kframe:annotation:LATEST_VERSION_HERE")
}

// your build logic
```

##### Groovy DSL

```groovy
import me.lusory.kframe.gradle.plugin.AnnotationProcessorMode

plugins {
    id 'me.lusory.kframe' version 'LATEST_VERSION_HERE'
    // make sure to also have the kotlin gradle plugin applied
    id 'org.jetbrains.kotlin.jvm' version '1.6.20'
}

repositories {
    mavenCentral()
    maven {
        url 'https://repo.lusory.dev/releases'
    }
    maven {
        url 'https://repo.lusory.dev/snapshots'
    }
}

// optional

kframe {
    // sets the main class name for generation and in the jar manifest
    mainFQClassName = 'kframe.Main'
    // or
    mainPackageName = 'kframe'
    mainClassName = 'Main'
    
    // should kotlin-stdlib and kotlin-reflect be applied to the project automatically (implementation)?
    applyKotlin = true

    // are you making a starter or an application?
    mode = AnnotationProcessorMode.APPLICATION
}

// your build logic
```

#### Without the plugin

Using the plugin is highly recommended, but you can also do it in plain Gradle.

##### Kotlin DSL

```kotlin
import java.util.Base64
import java.util.zip.ZipFile

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2" // only apply if you're making an application
    id("com.google.devtools.ksp") version "1.6.20-1.0.5" // pick the latest KSP version for your Kotlin version (kotlinver-kspver)
    // make sure to also have the kotlin gradle plugin applied
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenCentral()
    maven("https://repo.lusory.dev/releases")
    maven("https://repo.lusory.dev/snapshots")
}

dependencies {
    implementation("me.lusory.kframe:core:LATEST_VERSION_HERE")
    ksp("me.lusory.kframe:annotation:LATEST_VERSION_HERE")

    // kotlin-stdlib and kotlin-reflect
    // make compileOnly if making a starter
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}

kotlin {
    // add a generated folder to each kotlin source set
    sourceSets.forEach { sourceSet ->
        sourceSet.kotlin.srcDir("build/generated/ksp/${sourceSet.name}/kotlin")
    }
}

// only apply if you're making an application
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "kframe.Main" // sets the main class (generated) for jar outputs
    }
}

afterEvaluate {
    val members: MutableSet<String> = mutableSetOf()
    val classes: MutableSet<String> = mutableSetOf()
    val inits: MutableSet<String> = mutableSetOf()
    configurations.getByName("compileClasspath").resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        ZipFile(artifact.file).use { zipFile ->
            zipFile.entries().iterator().forEach { entry ->
                if (entry.name.substringAfterLast('/') == "inject.properties") {
                    val props: Properties = Properties().also { it.load(zipFile.getInputStream(entry)) }
                    members.addAll((props["kframe.dependencyInjection.members"] as? String ?: "").split(',').toMutableList().apply { clearIfLogicallyEmpty() })
                    classes.addAll((props["kframe.dependencyInjection.classes"] as? String ?: "").split(',').toMutableList().apply { clearIfLogicallyEmpty() })
                    inits.addAll((props["kframe.dependencyInjection.inits"] as? String ?: "").split(',').toMutableList().apply { clearIfLogicallyEmpty() })
                }
            }
        }
    }

    ksp {
        // https://github.com/google/ksp/issues/154
        arg("kframe.dependencyInjection.members", members.joinToString(",").toBase64())
        arg("kframe.dependencyInjection.classes", classes.joinToString(",").toBase64())
        arg("kframe.dependencyInjection.inits", inits.joinToString(",").toBase64())
    }
}

fun MutableCollection<String>.clearIfLogicallyEmpty() = clearIf { it.isEmpty() }

inline fun <T> MutableCollection<T>.clearIf(predicate: (T) -> Boolean) {
    if (all(predicate)) {
        clear()
    }
}

fun String.toBase64(): String = Base64.getEncoder().encodeToString(encodeToByteArray())

// optional

ksp {
    // sets the main class name for generation
    arg("kframe.dependencyInjection.packageName", "kframe")
    arg("kframe.dependencyInjection.className", "Main")
    
    // if running an application
    arg("kframe.dependencyInjection.enabled", "true")
    // if making a starter
    // arg("kframe.injectProperties.enabled", "true")
}
```
