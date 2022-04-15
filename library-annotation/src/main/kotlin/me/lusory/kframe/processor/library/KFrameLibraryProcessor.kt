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

package me.lusory.kframe.processor.library

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*

/**
 * The annotation processor for generating inject.properties files.
 *
 * @param environment the processor environment, passed down from the provider
 *
 * @since 0.0.1
 */
class KFrameLibraryProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        val deps: MutableSet<KSFile> = mutableSetOf()

        val members: MutableSet<String> = mutableSetOf()
        val classes: MutableSet<String> = mutableSetOf()
        val listeners: Set<String> = resolver.getSymbolsWithAnnotation("me.lusory.kframe.inject.On")
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
            lines.add("members=${members.joinToString(",")}")
        }
        if (classes.isNotEmpty()) {
            lines.add("classes=${classes.joinToString(",")}")
        }
        if (listeners.isNotEmpty()) {
            lines.add("listeners=${listeners.joinToString(",")}")
        }

        environment.codeGenerator.createNewFile(Dependencies(true, *deps.toTypedArray()), "", "inject", "properties")
            .use { outputStream ->
                outputStream.write(lines.joinToString("\n").encodeToByteArray())
            }

        invoked = true

        return emptyList()
    }
}