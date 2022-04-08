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

import me.lusory.kframe.inject.Component
import me.lusory.kframe.inject.Exact
import me.lusory.kframe.util.InternalAPI

/**
 * A command line argument parsing API.
 *
 * Instances are immutable.
 *
 * See the documentation [here](https://docs.lusory.dev/kframe/latest/modules/core/#argument-parsing).
 *
 * @author zlataovce
 * @since 0.0.1
 */
interface ArgumentParser {
    /**
     * A [List] of option name - value pairs.
     *
     * The pair value can be null, only if the option has no specified value.
     */
    val args: List<Pair<String, String?>>

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
     */
    operator fun get(vararg names: String): String? = args.firstOrNull { arg -> names.any { arg.first == it } }?.second
}

/**
 * Provides an [ArgumentParser] instance for dependency injection. Should **not** be called manually.
 *
 * @suppress API for internal use
 */
@Component(name = "argumentParser")
@InternalAPI // use dependency injection to get this instance
fun argumentParser(@Exact(name = "args") args: Array<String>): ArgumentParser = ArgumentParserImpl(args)