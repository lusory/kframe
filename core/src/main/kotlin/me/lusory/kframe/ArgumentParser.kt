package me.lusory.kframe

import me.lusory.kframe.inject.Component
import me.lusory.kframe.inject.Exact
import me.lusory.kframe.util.InternalAPI

interface ArgumentParser {
    val args: List<Pair<String?, String?>>

    val size: Int
        get() = args.size

    val isEmpty: Boolean
        get() = args.isEmpty()

    val isSingle: Boolean
        get() = (isEmpty || size == 1) && args.getOrNull(0)?.first == null

    val single: String?
        get() = if (isSingle) args.getOrNull(0)?.second else throw RuntimeException("Not a single")

    operator fun get(name: String): String? = args.firstOrNull { it.first == name }?.second
}

@Component(name = "argumentParser")
@InternalAPI // use dependency injection to get this instance
fun argumentParser(@Exact(name = "args") args: Array<String>): ArgumentParser = ArgumentParserImpl(args)