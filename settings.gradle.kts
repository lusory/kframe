rootProject.name = "kframe"

fun registerCombinedModule(module: String, vararg submodules: String) {
    submodules.forEach { submodule ->
        include("$module-$submodule")
        project(":$module-$submodule").projectDir = file("$module/$submodule")
    }
}

include(
    "core",
    "annotation",
    "plugin"
)

registerCombinedModule("ktorm", "core", "mysql")
registerCombinedModule("ktor", "server", "client")
registerCombinedModule("grpc", "core", "gradle")
