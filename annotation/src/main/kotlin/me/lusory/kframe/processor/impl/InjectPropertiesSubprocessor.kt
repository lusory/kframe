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
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import me.lusory.kframe.processor.KFrameSubprocessor
import me.lusory.kframe.processor.ProcessorPriority

/**
 * A subprocessor for generating inject.properties files.
 *
 * @since 0.0.1
 */
class InjectPropertiesSubprocessor : KFrameSubprocessor {
    override val priority: Int = ProcessorPriority.INTERNAL_LOW

    override fun process(environment: SymbolProcessorEnvironment, resolver: Resolver) {
        val deps: MutableSet<KSFile> = mutableSetOf()

        val members: MutableSet<String> = mutableSetOf()
        val classes: MutableSet<String> = mutableSetOf()
        val inits: Set<String> = resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.Init")
            .filter { it is KSFunctionDeclaration && (it.functionKind == FunctionKind.TOP_LEVEL || it.functionKind == FunctionKind.MEMBER) }
            .also { l -> deps.addAll(l.map { it.containingFile!! }) }
            .map { (it as KSFunctionDeclaration).qualifiedName!!.asString() }
            .toSet()

        resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.Component")
            .forEach { symbol ->
                if (symbol is KSFunctionDeclaration && (symbol.functionKind == FunctionKind.TOP_LEVEL || symbol.functionKind == FunctionKind.MEMBER)) {
                    deps.add(symbol.containingFile!!)
                    members.add(symbol.qualifiedName!!.asString())
                } else if (symbol is KSClassDeclaration) {
                    deps.add(symbol.containingFile!!)
                    classes.add(symbol.qualifiedName!!.asString())
                }
            }

        val lines: MutableList<String> = mutableListOf()

        if (members.isNotEmpty()) {
            lines.add("kframe.dependencyInjection.members=${members.joinToString(",")}")
        }
        if (classes.isNotEmpty()) {
            lines.add("kframe.dependencyInjection.classes=${classes.joinToString(",")}")
        }
        if (inits.isNotEmpty()) {
            lines.add("kframe.dependencyInjection.inits=${inits.joinToString(",")}")
        }

        environment.codeGenerator.createNewFile(Dependencies(true, *deps.toTypedArray()), "", "inject", "properties")
            .use { outputStream ->
                outputStream.write(lines.joinToString("\n").encodeToByteArray())
            }
    }
}