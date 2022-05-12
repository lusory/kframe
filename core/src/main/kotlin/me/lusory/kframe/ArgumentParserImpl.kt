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

import me.lusory.kframe.exceptions.ArgumentParseException

internal class ArgumentParserImpl(
    rawArgs: Array<String>,
    override val args: MutableList<Argument> = mutableListOf()
) : ArgumentParser {
    init {
        if (rawArgs.isNotEmpty()) {
            // name - isLong
            var lastArg: Pair<String, Boolean>? = null

            for (arg0: String in rawArgs) {
                var arg = arg0
                val isLong: Boolean = arg.startsWith("--")

                if (arg == "--" || arg == "-") {
                    throw ArgumentParseException("Invalid argument $arg")
                }
                arg = when {
                    isLong -> arg.substring(2)
                    arg.startsWith('-') -> arg.substring(1)
                    else -> {
                        if (lastArg == null) {
                            throw ArgumentParseException("Invalid argument $arg")
                        }

                        args += Argument(
                            lastArg.first,
                            arg.trimQuotes(),
                            lastArg.second
                        )

                        lastArg = null
                        continue
                    }
                }

                if (lastArg != null) {
                    args += Argument(
                        lastArg.first,
                        null,
                        lastArg.second
                    )

                    lastArg = null
                }

                if (arg.contains('=')) {
                    args += Argument(
                        arg.substringBefore('='),
                        arg.substringAfter('=').trimQuotes(),
                        isLong
                    )
                } else {
                    lastArg = arg to isLong
                }
            }

            if (lastArg != null) {
                args += Argument(
                    lastArg.first,
                    null,
                    lastArg.second
                )
            }
        }
    }

    private fun String.trimQuotes(): String = trim('\'', '"')
}