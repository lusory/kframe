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

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import me.lusory.kframe.processor.exceptions.CircularDependencyException
import me.lusory.kframe.processor.exceptions.DependencyResolveException

@OptIn(KotlinPoetKspPreview::class)
class KFrameProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return listOf()
        }

        val builder: FileSpec.Builder = FileSpec.builder("kframe", "Main")
            .addAnnotation(
                AnnotationSpec.builder(JvmName::class)
                    .addMember("name = %S", "Main")
                    .build()
            )

        val deps: MutableSet<KSFile> = mutableSetOf()

        val mainBuilder: FunSpec.Builder = FunSpec.builder("main")
            .addParameter("args", String::class, KModifier.VARARG)
            .beginControlFlow(
                "val context: %T = %M",
                ClassName("me.lusory.kframe.inject", "ApplicationContext"),
                MemberName("me.lusory.kframe.inject", "buildContext")
            )

        var varCount = 0
        val vars: MutableMap<String, MutableList<String>> = mutableMapOf()
        val backlog: MutableList<Pair<KSClassDeclaration, KSFunctionDeclaration>> = mutableListOf()

        resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.Component")
            .forEach { symbol ->
                if (symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS && symbol.isAccessible()) {
                    symbol.containingFile?.let { deps.add(it) }

                    val ctor: KSFunctionDeclaration = selectConstructor(symbol)
                    val params: MutableList<String> = mutableListOf()

                    val className: String = symbol.qualifiedName!!.asString()

                    environment.logger.info("Processing $className...")

                    for (param: KSValueParameter in ctor.parameters) {
                        val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                            ?.let { vars[it] }
                            ?.let { it[0] }

                        if (varName != null) {
                            params.add(varName)
                        } else {
                            environment.logger.info("Adding $className to backlog (unknown parameter).")
                            backlog.add(Pair(symbol, ctor))
                            return@forEach
                        }
                    }

                    val type: ClassName = ClassName.bestGuess(className)
                    val varName = "var${varCount++}"
                    mainBuilder.addStatement("val $varName: %T = %T(${params.joinToString(", ")})", type, type)
                    mainBuilder.addStatement("instance($varName)")

                    vars.getOrPut(className) { mutableListOf() }.add(varName)

                    environment.logger.info("Processed $className.")
                }
            }

        while (backlog.isNotEmpty()) {
            var modified = false
            val iter = backlog.listIterator()
            loop@while (iter.hasNext()) {
                val pair = iter.next()
                val classDeclaration: KSClassDeclaration = pair.first
                val ctor: KSFunctionDeclaration = pair.second

                val params: MutableList<String> = mutableListOf()

                val className: String = classDeclaration.qualifiedName!!.asString()

                environment.logger.info("Processing $className from backlog...")

                for (param: KSValueParameter in ctor.parameters) {
                    val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                        ?.let { vars[it] }
                        ?.let { it[0] }

                    if (varName != null) {
                        params.add(varName)
                    } else {
                        continue@loop
                    }
                }

                val type: ClassName = ClassName.bestGuess(className)
                val varName = "var${varCount++}"
                mainBuilder.addStatement("val $varName: %T = %T(${params.joinToString(", ")})", type, type)
                mainBuilder.addStatement("instance($varName)")

                vars.getOrPut(className) { mutableListOf() }.add(varName)

                modified = true
                iter.remove()

                environment.logger.info("Processed $className from backlog.")
            }

            if (!modified) {
                throw CircularDependencyException(backlog.joinToString(", "))
            }
        }

        builder.addFunction(mainBuilder.endControlFlow().build())
            .build()
            .writeTo(environment.codeGenerator, true, deps)

        environment.logger.info("Created kframe.Main.")

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
        val simpleName: String = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1)

        return annotations.any {
            it.shortName.getShortName() == simpleName
                    && it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
        }
    }

    private fun KSModifierListOwner.isAccessible(): Boolean =
        modifiers.none { it == Modifier.PRIVATE || it == Modifier.PROTECTED || it == Modifier.INTERNAL }
}