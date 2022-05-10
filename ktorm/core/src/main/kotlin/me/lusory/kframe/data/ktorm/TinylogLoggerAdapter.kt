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

package me.lusory.kframe.data.ktorm

import org.tinylog.kotlin.Logger

/**
 * A [org.ktorm.logging.Logger] implementation for [tinylog](https://github.com/tinylog-org/tinylog).
 *
 * @since 0.0.1
 */
class TinylogLoggerAdapter : org.ktorm.logging.Logger {
    override fun debug(msg: String, e: Throwable?) {
        if (e != null) {
            Logger.debug(e, msg)
        } else {
            Logger.debug(msg)
        }
    }

    override fun error(msg: String, e: Throwable?) {
        if (e != null) {
            Logger.error(e, msg)
        } else {
            Logger.error(msg)
        }
    }

    override fun info(msg: String, e: Throwable?) {
        if (e != null) {
            Logger.info(e, msg)
        } else {
            Logger.info(msg)
        }
    }

    override fun trace(msg: String, e: Throwable?) {
        if (e != null) {
            Logger.trace(e, msg)
        } else {
            Logger.trace(msg)
        }
    }

    override fun warn(msg: String, e: Throwable?) {
        if (e != null) {
            Logger.warn(e, msg)
        } else {
            Logger.warn(msg)
        }
    }

    override fun isDebugEnabled(): Boolean = Logger.isDebugEnabled()

    override fun isErrorEnabled(): Boolean = Logger.isErrorEnabled()

    override fun isInfoEnabled(): Boolean = Logger.isInfoEnabled()

    override fun isTraceEnabled(): Boolean = Logger.isTraceEnabled()

    override fun isWarnEnabled(): Boolean = Logger.isWarnEnabled()
}