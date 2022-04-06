package me.lusory.kframe

import me.lusory.kframe.exceptions.ArgumentParseException

internal class ArgumentParserImpl(
    rawArgs: Array<String>,
    override val args: MutableList<Pair<String?, String?>> = mutableListOf()
) : ArgumentParser {
    init {
        if (rawArgs.isEmpty()) {
            // do nothing
        } else if (rawArgs.size == 1) {
            args.add(null to rawArgs[0].trimQuotes())
        } else {
            var lastArg: String? = null

            for (arg0: String in rawArgs) {
                var arg = arg0

                if (arg == "--" || arg == "-") {
                    throw ArgumentParseException("Invalid argument $arg")
                }
                arg = when {
                    arg.startsWith("--") -> arg.substring(3)
                    arg.startsWith('-') -> arg.substring(2)
                    else -> {
                        if (lastArg == null) {
                            throw ArgumentParseException("Invalid argument $arg")
                        }

                        args.add(lastArg to arg.trimQuotes())
                        lastArg = null
                        continue
                    }
                }

                if (lastArg != null) {
                    args.add(lastArg to null)
                    lastArg = null
                }

                if (arg.contains('=')) {
                    args.add(arg.substringBefore('=') to arg.substringAfter('=').trimQuotes())
                } else {
                    lastArg = arg
                }
            }
        }
    }
}

fun String.trimQuotes(): String = trim('\'', '"')