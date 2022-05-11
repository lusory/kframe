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
 * The column's default value represented by a [ScalarExpression].
 */
@Suppress("UNCHECKED_CAST")
val <C : Any> Column<C>.default: ScalarExpression<C>?
    get() = table.data.columnDefault[name] as? ScalarExpression<C>

/**
 * The column nullability state (whether it can be null or not).
 */
val <C : Any> Column<C>.notNull: Boolean
    get() = this.name in table.data.columnNotNull

/**
 * The column size restriction (for varchar and possibly more).
 */
val <C : Any> Column<C>.size: Int?
    get() = table.data.columnSize[name]

/**
 * The column auto increment state (whether it should automatically increment itself or not).
 */
val <C : Any> Column<C>.autoIncrement: Boolean
    get() = this.name in table.data.columnAutoIncrement

/**
 * Enables auto increment on this column.
 *
 * @return this column
 */
fun <C : Number> Column<C>.autoIncrement(): Column<C> = apply { table.data.columnAutoIncrement.add(name) }

/**
 * Sets the default value for this column.
 *
 * @param value the default value
 * @return this column
 */
fun <C : Any> Column<C>.default(value: C): Column<C> = default(ArgumentExpression(value, sqlType))

/**
 * Sets the default value for this column as a [ScalarExpression].
 *
 * @param expression the default expression
 * @return this column
 */
fun <C : Any> Column<C>.default(expression: ScalarExpression<C>): Column<C> = apply { table.data.columnDefault[name] = expression }

/**
 * Adds a 'unique' constraint to this column.
 *
 * @return this column
 */
fun <C : Any> Column<C>.unique(): Column<C> = apply {
    table.data.constraints["${table.catalog}_${table.schema}_${table.tableName}_unique_$name"] = UniqueConstraint(across = listOf(this))
}

/**
 * Adds a foreign key constraint to this column.
 *
 * @return this column
 */
fun <C : Any> Column<C>.foreignKey(
    to: BaseTable<*>,
    @Suppress("UNCHECKED_CAST")
    on: Column<C> = to.primaryKeys.singleOrNull() as? Column<C> ?: error("Foreign key cannot be defined this way if there are multiple primary keys on the other")
): Column<C> = apply {
    table.data.constraints["${table.catalog}_${table.schema}_${table.tableName}_fk_$name"] =
        ForeignKeyConstraint(to = to, correspondence = mapOf(this to on))
}

/**
 * Restricts a column from being set to null, use [notNull] to see the state.
 *
 * @return this column
 */
fun <C : Any> Column<C>.notNull(): Column<C> = apply { table.data.columnNotNull.add(name) }

/**
 * Unrestricts a column from being set to null, use [notNull] to see the state.
 *
 * @return this column
 */
fun <C : Any> Column<C>.nullable(): Column<C> = apply { table.data.columnNotNull.remove(name) }

/**
 * Sets the size of a column, use [size] to see the value.
 *
 * @param set the column size
 * @return this column
 */
fun Column<String>.size(set: Int): Column<String> = apply { table.data.columnSize[name] = set }