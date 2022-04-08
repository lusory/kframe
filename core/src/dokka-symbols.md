# Package me.lusory.kframe

This is the root package of kframe, all modules will be subpackages of this.

# Package me.lusory.kframe.exceptions

This is the package with kframe-specific exceptions, a part of the core module.

# Package me.lusory.kframe.inject

This is the package with the dependency injection API, a part of the core module.

# Package me.lusory.kframe.util

This is the package with miscellaneous utilities, a part of the core module.

# Module core

This module serves as a base for an application, carrying the essential classes.

## Features
- Compile-time dependency injection
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