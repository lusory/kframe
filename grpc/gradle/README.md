# Package me.lusory.kframe.gradle.plugin.grpc

gRPC Gradle integration root package, a part of the grpc-gradle module

# Module grpc-gradle

Additional Gradle support for grpc-core

This module can be applied as a buildscript classpath dependency:

```kt
import me.lusory.kframe.gradle.plugin.grpc.configureGrpc

buildscript {
    dependencies {
        classpath(group = "me.lusory.kframe.net", name = "grpc-gradle", version = "LATEST_VERSION_HERE")
    }
}

// your build logic

configureGrpc()
```