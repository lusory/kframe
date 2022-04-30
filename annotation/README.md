# Package me.lusory.kframe.processor

Annotation processor root package, a part of the annotation module

# Package me.lusory.kframe.processor.impl

Implementations of subprocessors, a part of the annotation

# Package me.lusory.kframe.processor.exceptions

Annotation processor specific exceptions, a part of the annotation module

# Module annotation

A Kotlin Symbol Processing (KSP) annotation processor for compile-time logic generation

Currently there are two built-in subprocessors:
 - [DependencyInjectionSubprocessor]
 - [InjectPropertiesSubprocessor]

Custom ones can be added via Java's [ServiceLoader] API, by creating a service of type [KFrameSubprocessor].

By default, a `kframe.transformedClassName.enabled` KSP arg needs to be true for the subprocessor to be run (`transformedClassName` being the subprocessor class name with the first occurrence of `Subprocessor` removed and the first letter uncapitalized).