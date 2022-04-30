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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import java.util.*

/**
 * The annotation processor for KFrame.
 *
 * @param environment the processor environment, passed down from the provider
 *
 * @since 0.0.1
 */
class KFrameProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private var invoked: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        environment.logger.info("Loading subprocessors...")
        // specifying the class loader is important
        ServiceLoader.load(KFrameSubprocessor::class.java, javaClass.classLoader)
            .sortedBy { it.priority }
            .forEach {
                if (it.shouldRun(environment)) {
                    environment.logger.info("Invoking ${it.javaClass.name}...")
                    it.process(environment, resolver)
                } else {
                    environment.logger.info("Skipping ${it.javaClass.name}...")
                }
            }

        invoked = true
        return emptyList()
    }
}