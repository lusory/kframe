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

package me.lusory.kframe.data.ktorm.sql

import org.ktorm.expression.ArgumentExpression
import org.ktorm.expression.ScalarExpression
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

/**
 * Returns the default value expression for a column.
 */
@Suppress("UNCHECKED_CAST")
val <C : Any> Column<C>.default: ScalarExpression<C>?
    get() = table.data.columnDefault[name] as? ScalarExpression<C>

/**
 * Returns whether a column is restricted from being set to NULL.
 */
val <C : Any> Column<C>.notNull: Boolean
    get() = this.name in table.data.columnNotNull

/**
 * Returns the size of a column.
 */
val <C : Any> Column<C>.size: Int?
    get() = table.data.columnSize[name]

/**
 * Returns whether a column has auto increment enabled.
 */
val <C : Any> Column<C>.autoIncrement: Boolean
    get() = this.name in table.data.columnAutoIncrement

/**
 * Enables auto increment on a column.
 */
fun <C : Number> Column<C>.autoIncrement(autoIncrement: Boolean = true): Column<C> = apply {
    if (autoIncrement) table.data.columnAutoIncrement.add(name) else table.data.columnAutoIncrement.remove(name)
}

/**
 * Sets the default value for a column.
 */
fun <C : Any> Column<C>.default(value: C): Column<C> = default(ArgumentExpression(value, sqlType))

/**
 * Sets the default value for a column as an expression.
 */
fun <C : Any> Column<C>.default(expression: ScalarExpression<C>? = null): Column<C> = apply {
    if (expression != null) table.data.columnDefault[name] = expression else table.data.columnDefault.remove(name)
}

/**
 * Adds a constraint that this column must be unique across rows.
 */
fun <C : Any> Column<C>.unique(): Column<C> = apply {
    table.data.constraints["${table.catalog}_${table.schema}_${table.tableName}_unique_$name"] =
        UniqueConstraint(across = listOf(this))
}

/**
 * Adds a foreign key constraint to this column.
 */
fun <C : Any> Column<C>.foreignKey(
    to: BaseTable<*>,
    @Suppress("UNCHECKED_CAST")
    on: Column<C> = to.primaryKeys.singleOrNull() as? Column<C> ?: throw IllegalArgumentException("Foreign key cannot be defined this way if there are multiple primary keys on the other")
): Column<C> = apply {
    table.data.constraints["${table.catalog}_${table.schema}_${table.tableName}_fk_$name"] =
        ForeignKeyConstraint(to = to, correspondence = mapOf(this to on))
}

/**
 * Restricts a column from being set to null.
 */
fun <C : Any> Column<C>.notNull(notNull: Boolean = true): Column<C> = apply {
    if (notNull) table.data.columnNotNull.add(name) else table.data.columnNotNull.remove(name)
}

/**
 * Unrestricts a column from being set to null.
 */
fun <C : Any> Column<C>.nullable(): Column<C> = apply { table.data.columnNotNull.remove(name) }

/**
 * Sets the size of a column.
 */
fun Column<String>.size(set: Int): Column<String> = apply { table.data.columnSize[name] = set }