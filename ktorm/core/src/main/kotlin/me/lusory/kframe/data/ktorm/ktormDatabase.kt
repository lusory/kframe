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

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.lusory.kframe.data.ktorm.sql.detectMixinDialectImplementation
import org.ktorm.database.Database
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

/**
 * Creates a [Database] instance with a [HikariDataSource].
 *
 * @param block the data source configurator
 * @return the database
 */
inline fun database(block: HikariConfig.() -> Unit): Database = Database.connect(
    HikariDataSource(HikariConfig().apply(block)),
    dialect = detectMixinDialectImplementation(),
    logger = TinylogLoggerAdapter()
)

/**
 * Determines if the database contains the supplied table.
 *
 * @param table the table
 * @return is the table in the database?
 */
operator fun Database.contains(table: BaseTable<*>): Boolean = useConnection { connection ->
    connection.metaData.getTables(table.catalog, table.schema, table.tableName, null).next()
}

/**
 * Determines if the database contains the supplied table-bound column.
 *
 * @param column the column
 * @return is the column in a table in the database?
 */
operator fun Database.contains(column: Column<*>): Boolean = useConnection { connection ->
    connection.metaData.getColumns(column.table.catalog, column.table.schema, column.table.tableName, column.name).next()
}