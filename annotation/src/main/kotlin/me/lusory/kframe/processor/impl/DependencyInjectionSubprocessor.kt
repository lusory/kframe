/*
 * This file is part of kframe, licensed under the Apache License, Version 2.0 (the "License").
 *
 * Copyright (c) 2022-present lusory contributors
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.lusory.kframe.processor.impl

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import me.lusory.kframe.processor.*
import me.lusory.kframe.processor.exceptions.DependencyResolveException

/**
 * A subprocessor for dependency injection (me.lusory.kframe.inject).
 *
 * @since 0.0.1
 */
@OptIn(KotlinPoetKspPreview::class)
class DependencyInjectionSubprocessor : KFrameSubprocessor {
    private lateinit var environment: SymbolProcessorEnvironment
    override val priority: Int = ProcessorPriority.INTERNAL_LOW

    override fun process(environment: SymbolProcessorEnvironment, resolver: Resolver) {
        this.environment = environment

        val packageName: String = environment.options["kframe.dependencyInjection.packageName"] ?: "kframe"
        val className: String = environment.options["kframe.dependencyInjection.className"] ?: "Main"

        val builder: FileSpec.Builder = FileSpec.builder(packageName, className)
            .jvmName(className)

        val mainBuilder: FunSpec.Builder = FunSpec.builder("main")
            .addParameter("args", ARRAY.parameterizedBy(STRING))
            .beginControlFlow("%M", MemberName("me.lusory.kframe.inject", "applicationContext"))

        val deps: MutableSet<KSFile> = mutableSetOf()

        var varCount = 0
        // class name -> [var name, is non-singleton, qualifier]
        val vars: MutableMap<String, MutableList<Triple<String, Boolean, String?>>> = mutableMapOf(
            "kotlin.Array" to mutableListOf(
                Triple("args", false, "args") // args are available for injection with qualifier
            )
        )
        val backlog: MutableList<BacklogItem> = mutableListOf()

        resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.Component").toMutableList()
            .apply {
                // TODO: replace with https://github.com/google/ksp/issues/431
                addAll(
                    (environment.options["kframe.dependencyInjection.classes"] ?: "").fromBase64().split(',').toMutableList().apply { clearIfLogicallyEmpty() }
                        .map { resolver.getClassDeclarationByName(it) ?: throw RuntimeException("Could not resolve component $it") }
                )
                addAll(
                    (environment.options["kframe.dependencyInjection.members"] ?: "").fromBase64().split(',').toMutableList().apply { clearIfLogicallyEmpty() }
                        .flatMap { resolver.getFunctionDeclarationsByName(resolver.getKSNameFromString(it), includeTopLevel = true) }
                )
            }
            .forEach { symbol ->
                if (((symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS) || (symbol is KSFunctionDeclaration && symbol.functionKind == FunctionKind.TOP_LEVEL)) && (symbol as KSModifierListOwner).isAccessible) {
                    symbol.containingFile?.let { deps.add(it) }

                    val func: KSFunctionDeclaration =
                        if (symbol is KSClassDeclaration) selectConstructor(symbol) else symbol as KSFunctionDeclaration
                    val params: MutableList<String> = mutableListOf()

                    val elemDeclaration: KSClassDeclaration =
                        if (symbol is KSClassDeclaration) symbol else func.returnType!!.resolve().declaration as KSClassDeclaration
                    val symbolClassName: String = elemDeclaration.qualifiedName!!.asString()
                    val isNonSingleton: Boolean = symbol.isAnnotationPresent("me.lusory.kframe.inject.NonSingleton")

                    environment.logger.info("Processing $symbolClassName...")

                    for (param: KSValueParameter in func.parameters) {
                        val exactType: String? = param.getAnnotationsByType("me.lusory.kframe.inject.Exact")
                            .firstOrNull()
                            ?.let { (it.arguments.first { arg -> arg.name?.asString() == "name" }.value as String).nullIfEmpty() }

                        val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                            ?.let { vars[it] }
                            ?.let { if (exactType != null) it.firstOrNull { triple -> triple.third == exactType } else it.first() }
                            ?.let { if (it.second) "${it.first}()" else it.first }

                        if (varName != null) {
                            params.add(varName)
                        } else {
                            environment.logger.info("Adding $symbolClassName to backlog (unknown parameter).")
                            backlog.add(BacklogItem(elemDeclaration, func, isNonSingleton, exactType))
                            return@forEach
                        }
                    }

                    val varName = "var${varCount++}"
                    if (func.isConstructor()) {
                        val type: ClassName = ClassName.bestGuess(symbolClassName)
                        mainBuilder.beginControlFlow(if (isNonSingleton) "val $varName: () -> %T = newComponentProvider" else "val $varName: %T = newComponent", type)
                            .addStatement("%T(${params.joinToString(", ")})", type)
                            .endControlFlow()
                    } else {
                        val methodType = MemberName(func.packageName.asString(), func.simpleName.asString())
                        mainBuilder.beginControlFlow(if (isNonSingleton) "val $varName: () -> %T = newComponentProvider" else "val $varName: %T = newComponent", elemDeclaration.toClassName())
                            .addStatement("%M(${params.joinToString(", ")})", methodType)
                            .endControlFlow()
                    }

                    vars.getOrPut(symbolClassName) { mutableListOf() }.add(Triple(
                        varName,
                        isNonSingleton,
                        symbol.getAnnotationsByType("me.lusory.kframe.inject.Component")
                            .first()
                            .let { (it.arguments.first { arg -> arg.name?.asString() == "name" }.value as String).nullIfEmpty() }
                    ))

                    environment.logger.info("Processed $symbolClassName.")
                }
            }

        while (backlog.isNotEmpty()) {
            var modified = false
            val iter = backlog.listIterator()
            loop@ while (iter.hasNext()) {
                val backlogItem = iter.next()
                val params: MutableList<String> = mutableListOf()

                val symbolClassName: String = backlogItem.classDecl.qualifiedName!!.asString()

                environment.logger.info("Processing $symbolClassName from backlog...")

                for (param: KSValueParameter in backlogItem.funcDecl.parameters) {
                    val exactType: String? = param.getAnnotationsByType("me.lusory.kframe.inject.Exact")
                        .firstOrNull()
                        ?.let { (it.arguments.first { arg -> arg.name?.asString() == "name" }.value as String).nullIfEmpty() }

                    val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                        ?.let { vars[it] }
                        ?.let { if (exactType != null) it.firstOrNull { triple -> triple.third == exactType } else it.first() }
                        ?.let { if (it.second) "${it.first}()" else it.first }

                    if (varName != null) {
                        params.add(varName)
                    } else {
                        continue@loop
                    }
                }

                val varName = "var${varCount++}"
                if (backlogItem.funcDecl.isConstructor()) {
                    val type: ClassName = ClassName.bestGuess(symbolClassName)
                    mainBuilder.beginControlFlow(if (backlogItem.isNonSingleton) "val $varName: () -> %T = newComponentProvider" else "val $varName: %T = newComponent", type)
                        .addStatement("%T(${params.joinToString(", ")})", type)
                        .endControlFlow()
                } else {
                    val methodType = MemberName(backlogItem.funcDecl.packageName.asString(), backlogItem.funcDecl.simpleName.asString())
                    mainBuilder.beginControlFlow(if (backlogItem.isNonSingleton) "val $varName: () -> %T = newComponentProvider" else "val $varName: %T = newComponent", backlogItem.classDecl.toClassName())
                        .addStatement("%M(${params.joinToString(", ")})", methodType)
                        .endControlFlow()
                }

                vars.getOrPut(symbolClassName) { mutableListOf() }.add(Triple(varName, backlogItem.isNonSingleton, backlogItem.qualifier))

                modified = true
                iter.remove()

                environment.logger.info("Processed $symbolClassName from backlog.")
            }

            if (!modified) {
                throw DependencyResolveException("Unsatisfied dependency (possible circular dependency), backlog: ${
                    backlog.joinToString(", ")
                }")
            }
        }

        val inits: List<KSAnnotated> = resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.Init").toMutableList().apply {
            // TODO: replace with https://github.com/google/ksp/issues/431
            addAll(
                (environment.options["kframe.dependencyInjection.inits"] ?: "").fromBase64().split(',').toMutableList().apply { clearIfLogicallyEmpty() }
                    .flatMap { resolver.getFunctionDeclarationsByName(resolver.getKSNameFromString(it), includeTopLevel = true) }
            )
            sortedBy { it.getAnnotationsByType("me.lusory.kframe.inject.Init").first().arguments.first { arg -> arg.name?.asString() == "priority" }.value as Int }
        }

        if (inits.isNotEmpty()) {
            mainBuilder.beginControlFlow("afterBuild { context ->")

            inits.forEach { mainBuilder.listenerCall(it, vars) }

            mainBuilder.endControlFlow()
        }

        mainBuilder.endControlFlow()

        builder.addFunction(mainBuilder.build())
            .indent("    ") // 4 space indent
            .addFileComment("This file was generated with KFrame. Do not edit, changes will be overwritten!")
            .build()
            .writeTo(environment.codeGenerator, true, deps)

        environment.logger.info("Created ${packageName}.${className}.")
    }

    private fun selectConstructor(classDeclaration: KSClassDeclaration): KSFunctionDeclaration {
        val ctors: Sequence<KSFunctionDeclaration> = classDeclaration.getConstructors()
        if (ctors.count() == 1) {
            return ctors.first()
        }
        return ctors.firstOrNull { it.isAnnotationPresent("me.lusory.kframe.inject.Autowired") && it.isAccessible }
            ?: throw DependencyResolveException("No autowiring constructor found for class " + classDeclaration.qualifiedName?.asString())
    }

    private fun FunSpec.Builder.listenerCall(symbol: KSAnnotated, vars: Map<String, MutableList<Triple<String, Boolean, String?>>>) {
        if (symbol is KSFunctionDeclaration) {
            if (symbol.functionKind == FunctionKind.TOP_LEVEL) {
                val memberName = MemberName(symbol.packageName.asString(), symbol.simpleName.asString())
                if (symbol.parameters.size == 1 && symbol.parameters[0].type.resolve().declaration.qualifiedName?.asString() == "me.lusory.kframe.inject.ApplicationContext") {
                    addStatement("%M(context)", memberName)
                } else if (symbol.parameters.isEmpty()) {
                    addStatement("%M()", memberName)
                } else {
                    throw IllegalArgumentException("@Init annotated methods must accept zero parameters or only one of type ApplicationContext")
                }
            } else if (symbol.functionKind == FunctionKind.MEMBER) {
                val parentClassName: String = (symbol.parentDeclaration as? KSClassDeclaration)!!.qualifiedName!!.asString()
                val memberVars = vars[parentClassName]
                    ?.filter { !it.second } // filter non-singletons

                if (memberVars == null) {
                    environment.logger.warn("Initializer method ${symbol.simpleName} found for non-component type $parentClassName")
                    return
                }

                for (memberVar in memberVars) {
                    if (symbol.parameters.size == 1 && symbol.parameters[0].type.resolve().declaration.qualifiedName?.asString() == "me.lusory.kframe.inject.ApplicationContext") {
                        addStatement("${memberVar.first}.${symbol.simpleName}(context)")
                    } else if (symbol.parameters.isEmpty()) {
                        addStatement("${memberVar.first}.${symbol.simpleName}()")
                    } else {
                        throw IllegalArgumentException("@Init annotated methods must accept zero parameters or only one of type ApplicationContext")
                    }
                }
            } else {
                throw UnsupportedOperationException("Only top-level or component member functions can be annotated with @Init")
            }
        }
    }

    private data class BacklogItem(
        val classDecl: KSClassDeclaration,
        val funcDecl: KSFunctionDeclaration,
        val isNonSingleton: Boolean,
        val qualifier: String?
    )
}