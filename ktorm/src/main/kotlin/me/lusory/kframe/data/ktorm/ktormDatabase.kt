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

import org.ktorm.database.Database
import org.tinylog.kotlin.Logger
import kotlin.reflect.KClass

/**
 * A DSL builder for [Database].
 *
 * @since 0.0.1
 */
interface DatabaseBuilder {
    /**
     * The JDBC connection string, must not be null on build.
     */
    var connectionUrl: String?

    /**
     * The database user, can be included in [connectionUrl].
     */
    var user: String?

    /**
     * The database password, can be included in [connectionUrl].
     */
    var password: String?

    /**
     * The driver class name, use [driver] to specify a driver class.
     */
    var driverClassName: String?

    /**
     * The driver class. Has no backing property, just a convenience accessor for [driverClassName].
     */
    var driver: KClass<*>
        get() = Class.forName(driverClassName ?: throw UnsupportedOperationException("Driver class name must be set")).kotlin
        set(value) {
            driverClassName = value.qualifiedName
        }

    /**
     * Builds the [Database].
     *
     * @return the database
     */
    fun build(): Database
}

internal class DatabaseBuilderImpl(
    override var connectionUrl: String? = null,
    override var user: String? = null,
    override var password: String? = null,
    override var driverClassName: String? = null
) : DatabaseBuilder {
    override fun build(): Database = Database.connect(
        connectionUrl ?: throw IllegalArgumentException("Connection URL not specified"),
        user = user,
        password = password,
        driver = driverClassName,
        logger = TinylogLoggerAdapter()
    )
}

internal class TinylogLoggerAdapter : org.ktorm.logging.Logger {
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

    override fun isDebugEnabled(): Boolean = Logger.isDebugEnabled()

    override fun isErrorEnabled(): Boolean = Logger.isErrorEnabled()

    override fun isInfoEnabled(): Boolean = Logger.isInfoEnabled()

    override fun isTraceEnabled(): Boolean = Logger.isTraceEnabled()

    override fun isWarnEnabled(): Boolean = Logger.isWarnEnabled()

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
}

/**
 * Creates a [Database] instance with a builder.
 *
 * @param block the builder mutator
 * @return the database
 */
fun database(block: DatabaseBuilder.() -> Unit): Database = DatabaseBuilderImpl().apply(block).build()