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
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
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
                    mainBuilder.addStatement("addInstance($varName)")

                    vars.getOrPut(className) { mutableListOf() }.add(varName)

                    environment.logger.info("Processed $className.")
                } else if (symbol is KSFunctionDeclaration && symbol.functionKind == FunctionKind.TOP_LEVEL) {
                    symbol.containingFile?.let { deps.add(it) }

                    val params: MutableList<String> = mutableListOf()

                    val returnType: KSType = symbol.returnType!!.resolve()
                    val className: String = returnType.declaration.qualifiedName!!.asString()

                    environment.logger.info("Processing method ${symbol.simpleName} with type $className...")

                    for (param: KSValueParameter in symbol.parameters) {
                        val varName: String? = param.type.resolve().declaration.qualifiedName?.asString()
                            ?.let { vars[it] }
                            ?.let { it[0] }

                        if (varName != null) {
                            params.add(varName)
                        } else {
                            environment.logger.info("Adding method ${symbol.simpleName} with type $className to backlog (unknown parameter).")
                            backlog.add(Pair(returnType.declaration as KSClassDeclaration, symbol))
                            return@forEach
                        }
                    }

                    val methodType = MemberName(symbol.packageName.asString(), symbol.simpleName.asString())
                    val varName = "var${varCount++}"
                    mainBuilder.addStatement("val $varName: %T = %M(${params.joinToString(", ")})", returnType.toClassName(), methodType)
                    mainBuilder.addStatement("addInstance($varName)")

                    vars.getOrPut(className) { mutableListOf() }.add(varName)

                    environment.logger.info("Processed method ${symbol.simpleName} with type $className.")
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

                val varName = "var${varCount++}"
                if (ctor.isConstructor()) {
                    val type: ClassName = ClassName.bestGuess(className)
                    mainBuilder.addStatement("val $varName: %T = %T(${params.joinToString(", ")})", type, type)
                    mainBuilder.addStatement("addInstance($varName)")
                } else {
                    val methodType = MemberName(ctor.packageName.asString(), ctor.simpleName.asString())
                    mainBuilder.addStatement("val $varName: %T = %M(${params.joinToString(", ")})", classDeclaration.toClassName(), methodType)
                    mainBuilder.addStatement("addInstance($varName)")
                }

                vars.getOrPut(className) { mutableListOf() }.add(varName)

                modified = true
                iter.remove()

                environment.logger.info("Processed $className from backlog.")
            }

            if (!modified) {
                throw CircularDependencyException(backlog.joinToString(", "))
            }
        }

        mainBuilder.endControlFlow()

        builder.addFunction(mainBuilder.build())
            .indent("    ") // 4 space indent
            .addFileComment("This file was generated with KFrame. Do not edit, changes will be overwritten!")
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