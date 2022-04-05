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

package me.lusory.kframe.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import me.lusory.kframe.processor.exceptions.DependencyResolveException

@OptIn(KotlinPoetKspPreview::class)
class KFrameProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return listOf()
        }

        val packageName: String = environment.options["packageName"] ?: "kframe"
        val className: String = environment.options["className"] ?: "Main"

        val builder: FileSpec.Builder = FileSpec.builder(packageName, className)
            .jvmName(className)

        val mainBuilder: FunSpec.Builder = FunSpec.builder("main")
            .addParameter("args", ARRAY.parameterizedBy(STRING))
            .beginControlFlow(
                "%M",
                // ClassName("me.lusory.kframe.inject", "ApplicationContext"),
                MemberName("me.lusory.kframe.inject", "buildContext")
            )

        val deps: MutableSet<KSFile> = mutableSetOf()

        var varCount = 0
        val vars: MutableMap<String, Pair<String, Boolean>> = mutableMapOf()
        val backlog: MutableList<Triple<KSClassDeclaration, KSFunctionDeclaration, Boolean>> = mutableListOf()

        resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.Component")
            .forEach { symbol ->
                if (((symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS) || (symbol is KSFunctionDeclaration && symbol.functionKind == FunctionKind.TOP_LEVEL)) && (symbol as KSModifierListOwner).isAccessible()) {
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
                        val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                            ?.let { vars[it] }
                            ?.let { if (it.second) "${it.first}()" else it.first }

                        if (varName != null) {
                            params.add(varName)
                        } else {
                            environment.logger.info("Adding $symbolClassName to backlog (unknown parameter).")
                            backlog.add(Triple(elemDeclaration, func, isNonSingleton))
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

                    vars[symbolClassName] = Pair(varName, isNonSingleton)

                    environment.logger.info("Processed $symbolClassName.")
                }
            }

        while (backlog.isNotEmpty()) {
            var modified = false
            val iter = backlog.listIterator()
            loop@ while (iter.hasNext()) {
                val triple = iter.next()
                val classDeclaration: KSClassDeclaration = triple.first

                val func: KSFunctionDeclaration = triple.second
                val params: MutableList<String> = mutableListOf()

                val symbolClassName: String = classDeclaration.qualifiedName!!.asString()

                environment.logger.info("Processing $symbolClassName from backlog...")

                for (param: KSValueParameter in func.parameters) {
                    val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                        ?.let { vars[it] }
                        ?.let { if (it.second) "${it.first}()" else it.first }

                    if (varName != null) {
                        params.add(varName)
                    } else {
                        continue@loop
                    }
                }

                val isNonSingleton = triple.third
                val varName = "var${varCount++}"
                if (func.isConstructor()) {
                    val type: ClassName = ClassName.bestGuess(symbolClassName)
                    mainBuilder.beginControlFlow(if (isNonSingleton) "val $varName: () -> %T = newComponentProvider" else "val $varName: %T = newComponent", type)
                        .addStatement("%T(${params.joinToString(", ")})", type)
                        .endControlFlow()
                } else {
                    val methodType = MemberName(func.packageName.asString(), func.simpleName.asString())
                    mainBuilder.beginControlFlow(if (isNonSingleton) "val $varName: () -> %T = newComponentProvider" else "val $varName: %T = newComponent", classDeclaration.toClassName())
                        .addStatement("%M(${params.joinToString(", ")})", methodType)
                        .endControlFlow()
                }

                vars[symbolClassName] = Pair(varName, isNonSingleton)

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

        mainBuilder.endControlFlow()

        builder.addFunction(mainBuilder.build())
            .indent("    ") // 4 space indent
            .addFileComment("This file was generated with KFrame. Do not edit, changes will be overwritten!")
            .build()
            .writeTo(environment.codeGenerator, true, deps)

        environment.logger.info("Created ${packageName}.${className}.")

        invoked = true
        return listOf()
    }

    private fun selectConstructor(classDeclaration: KSClassDeclaration): KSFunctionDeclaration {
        val ctors: Sequence<KSFunctionDeclaration> = classDeclaration.getConstructors()
        if (ctors.count() == 1) {
            return ctors.first()
        }
        return ctors.firstOrNull { it.isAnnotationPresent("me.lusory.kframe.inject.Autowired") && it.isAccessible() }
            ?: throw DependencyResolveException("No autowiring constructor found for class " + classDeclaration.qualifiedName?.asString())
    }

    private fun KSAnnotated.isAnnotationPresent(qualifiedName: String): Boolean {
        val simpleName: String = qualifiedName.substringAfterLast('.')

        return annotations.any {
            it.shortName.getShortName() == simpleName
                    && it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
        }
    }

    private fun KSModifierListOwner.isAccessible(): Boolean =
        modifiers.none { it == Modifier.PRIVATE || it == Modifier.PROTECTED || it == Modifier.INTERNAL }
}