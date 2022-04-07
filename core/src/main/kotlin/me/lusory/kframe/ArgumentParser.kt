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

    operator fun get(vararg names: String): String? = args.firstOrNull { arg -> names.any { arg.first == it } }?.second
}

@Component(name = "argumentParser")
@InternalAPI // use dependency injection to get this instance
fun argumentParser(@Exact(name = "args") args: Array<String>): ArgumentParser = ArgumentParserImpl(args)