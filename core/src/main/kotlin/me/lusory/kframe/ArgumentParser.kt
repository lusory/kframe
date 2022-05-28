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

package me.lusory.kframe

import me.lusory.kframe.inject.*
import me.lusory.kframe.util.InternalAPI
import me.lusory.kframe.util.getClasspathResource
import me.lusory.kframe.util.properties
import me.lusory.kframe.util.property
import java.util.*

/**
 * A command line argument parsing API.
 *
 * Instances are immutable.
 *
 * @since 0.0.1
 */
interface ArgumentParser {
    /**
     * A [List] of option name - value pairs.
     *
     * The value can be null, only if the option has no specified value.
     */
    val args: List<Argument>

    /**
     * The amount of parsed option name - value pairs.
     */
    val size: Int
        get() = args.size

    /**
     * Were any arguments processed?
     */
    val isEmpty: Boolean
        get() = args.isEmpty()

    /**
     * Gets the value of the first option whose name matches one of the supplied names.
     *
     * @param names the possible option names (e.g. a long and a short name)
     * @return the option value, null if no value was supplied
     */
    operator fun get(vararg names: String): String? = args.firstOrNull { arg -> names.any { arg.name == it } }?.value
}

/**
 * An immutable data-holding class for an option name and value.
 */
data class Argument(
    /**
     * The option name.
     */
    val name: String,
    /**
     * The option value, null if not supplied.
     */
    val value: String?,
    /**
     * Is this option specified as a long one? (prefixed with `--`, e.g. --kframe.stuff)
     */
    val isLong: Boolean
)

/**
 * Provides an [ArgumentParser] instance for dependency injection. Should **not** be called manually.
 *
 * @param args the application arguments from the `main` function
 * @return the [ArgumentParser] instance
 * @suppress API for internal use
 */
@Component(name = "argumentParser")
@InternalAPI(note = "use dependency injection to get this instance")
fun argumentParser(@Exact(name = "args") args: Array<String>): ArgumentParser = ArgumentParserImpl(args)

/**
 * Adds long arguments and a classpath configuration file to system properties.
 *
 * The classpath configuration file name can be supplied via the `kframe.configuration.classpath` property, default is `application.properties`.
 *
 * Properties provided as long arguments will take precedence before classpath configuration and other system properties.
 *
 * @param context the application context
 * @suppress API for internal use
 */
@Init(priority = InitPriority.INTERNAL_HIGH)
fun populateProperties(argParser: ArgumentParser) {
    val cmdProps = Properties()
    for (arg: Argument in argParser.args) {
        if (arg.isLong) {
            cmdProps[arg.name] = arg.value
        }
    }

    getClasspathResource(cmdProps["kframe.configuration.classpath"] as? String ?: property("kframe.configuration.classpath") ?: "application.properties")?.let { inputStream ->
        properties().load(inputStream)
    }

    // command line supplied properties should take precedence before the classpath configuration
    properties().putAll(cmdProps)
}