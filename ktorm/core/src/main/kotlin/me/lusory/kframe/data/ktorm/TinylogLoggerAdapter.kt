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

@file:Suppress("PrivatePropertyName")

package me.lusory.kframe.data.ktorm

import org.tinylog.Level
import org.tinylog.provider.LoggingProvider
import org.tinylog.provider.ProviderRegistry

/**
 * A [org.ktorm.logging.Logger] implementation for [tinylog](https://github.com/tinylog-org/tinylog).
 *
 * @since 0.0.1
 */
class TinylogLoggerAdapter : org.ktorm.logging.Logger {
    private val provider: LoggingProvider = ProviderRegistry.getLoggingProvider()

    // 1 is this
    // 2 is org.ktorm.logging.Logger$DefaultImpls
    // 3 is what we need
    private val STACKTRACE_DEPTH = 3

    private val MINIMUM_LEVEL_COVERS_TRACE = isCoveredByMinimumLevel(Level.TRACE)
    private val MINIMUM_LEVEL_COVERS_DEBUG = isCoveredByMinimumLevel(Level.DEBUG)
    private val MINIMUM_LEVEL_COVERS_INFO = isCoveredByMinimumLevel(Level.INFO)
    private val MINIMUM_LEVEL_COVERS_WARN = isCoveredByMinimumLevel(Level.WARN)
    private val MINIMUM_LEVEL_COVERS_ERROR = isCoveredByMinimumLevel(Level.ERROR)

    private fun isCoveredByMinimumLevel(level: Level): Boolean = provider.getMinimumLevel(null).ordinal <= level.ordinal

    override fun debug(msg: String, e: Throwable?) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, e, null, msg)
        }
    }

    override fun error(msg: String, e: Throwable?) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, e, null, msg)
        }
    }

    override fun info(msg: String, e: Throwable?) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, e, null, msg)
        }
    }

    override fun trace(msg: String, e: Throwable?) {
        if (MINIMUM_LEVEL_COVERS_TRACE) {
            provider.log(STACKTRACE_DEPTH, null, Level.TRACE, e, null, msg)
        }
    }

    override fun warn(msg: String, e: Throwable?) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, e, null, msg)
        }
    }

    override fun isDebugEnabled(): Boolean = MINIMUM_LEVEL_COVERS_DEBUG && provider.isEnabled(STACKTRACE_DEPTH, null, Level.DEBUG)

    override fun isErrorEnabled(): Boolean = MINIMUM_LEVEL_COVERS_ERROR && provider.isEnabled(STACKTRACE_DEPTH, null, Level.ERROR)

    override fun isInfoEnabled(): Boolean = MINIMUM_LEVEL_COVERS_INFO && provider.isEnabled(STACKTRACE_DEPTH, null, Level.INFO)

    override fun isTraceEnabled(): Boolean = MINIMUM_LEVEL_COVERS_TRACE && provider.isEnabled(STACKTRACE_DEPTH, null, Level.TRACE)

    override fun isWarnEnabled(): Boolean = MINIMUM_LEVEL_COVERS_WARN && provider.isEnabled(STACKTRACE_DEPTH, null, Level.WARN)
}