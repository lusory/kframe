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

import org.ktorm.expression.*
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType

/**
 * Creates a schema.
 *
 * @property name the name of the schema
 * @property ifNotExists if true, do not error if the schema already exists
 */
data class CreateSchemaExpression(
    val name: String,
    val ifNotExists: Boolean = false,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Drops a schema.
 *
 * @property name the name of the schema
 */
data class DropSchemaExpression(
    val name: String,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Creates an index.
 *
 * @property name the name of the index
 * @property on the table on which to create an index
 * @property columns the list of columns to index, ordered
 */
data class CreateIndexExpression(
    val name: String,
    val on: TableReferenceExpression,
    val columns: List<ColumnReferenceExpression>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Drops an index.
 *
 * @property name the name of the index.
 * @property on the table on which to drop the index.
 */
data class DropIndexExpression(
    val name: String,
    val on: TableReferenceExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Creates a view.
 *
 * @property name the name of the view
 * @property query the query this view shows the results of
 * @property orReplace if true, just replace any view which already has this name
 */
data class CreateViewExpression(
    val name: TableReferenceExpression,
    val query: SelectExpression,
    val orReplace: Boolean = false,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Drops a view.
 *
 * @property name the name of the view
 */
data class DropViewExpression(
    val name: TableReferenceExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Creates a table.
 *
 * @property name the name of the table
 * @property columns the columns the table needs
 * @property constraints the constraints the table has
 * @property ifNotExists if true, ignores this operation if the table already exists
 */
data class CreateTableExpression(
    val name: TableReferenceExpression,
    val columns: List<ColumnDeclarationExpression<*>>,
    val constraints: Map<String, TableConstraintExpression> = emptyMap(),
    val ifNotExists: Boolean = false,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Drops a table.
 *
 * @property table the name of the table
 */
data class DropTableExpression(
    val table: TableReferenceExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Truncates a table, removing all of its rows.
 *
 * @property table the name of the table
 */
data class TruncateTableExpression(
    val table: TableReferenceExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Adds a column to a table.
 *
 * @property table the table the add a column to
 * @property column the declaration of the column
 */
data class AlterTableAddExpression(
    val table: TableReferenceExpression,
    val column: ColumnDeclarationExpression<*>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Drops a column from a table.
 *
 * @property table the table to remove a column from
 * @property column the column to drop
 */
data class AlterTableDropColumnExpression(
    val table: TableReferenceExpression,
    val column: ColumnReferenceExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * A modification to an existing column.
 *
 * @property table the table the column belongs to
 * @property column the column to modify
 * @property newType the new type of the column
 * @property size the new size of the column
 * @property notNull whether there is a restriction on the column's value being null
 */
data class AlterTableModifyColumnExpression(
    val table: TableReferenceExpression,
    val column: ColumnReferenceExpression,
    val newType: SqlType<*>,
    val size: Int? = null,
    val notNull: Boolean = false,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Sets the default value for a column.
 *
 * @property table the table the column belongs to
 * @property column the column to modify
 * @property default the old default of the column
 */
data class AlterTableSetDefaultExpression(
    val table: TableReferenceExpression,
    val column: ColumnReferenceExpression,
    val default: ScalarExpression<*>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Removes the default value for a column.
 *
 * @property table the table the column belongs to
 * @property column the column to modify
 */
data class AlterTableDropDefaultExpression(
    val table: TableReferenceExpression,
    val column: ColumnReferenceExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Adds a constraint to a table.
 *
 * @property table the table the column belongs to
 * @property constraintName the name of the new constraint
 * @property tableConstraint the constraint to add
 */
data class AlterTableAddConstraintExpression(
    val table: TableReferenceExpression,
    val constraintName: String,
    val tableConstraint: TableConstraintExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Drops a constraint from a table.
 *
 * @property table the table the column belongs to
 * @property constraintName the name of the constraint to remove
 */
data class AlterTableDropConstraintExpression(
    val table: TableReferenceExpression,
    val constraintName: String,
    val tableConstraint: TableConstraintExpression,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * A declaration of a column for a table.
 *
 * @property name the name of the column.
 * @property sqlType the type of the column.
 * @property size the size of the column.
 * @property notNull whether this column has a restriction on null.
 * @property default the default value of the column.
 * @property autoIncrement whether this column should auto increment.
 */
data class ColumnDeclarationExpression<T : Any>(
    val name: String,
    val sqlType: SqlType<T>,
    val size: Int? = null,
    val notNull: Boolean = false,
    val default: ScalarExpression<out Any>? = null,
    val autoIncrement: Boolean = false,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : SqlExpression()

/**
 * Makes a [ColumnDeclarationExpression] from a column.
 */
fun <T : Any> Column<T>.asDeclarationExpression(): ColumnDeclarationExpression<T> = ColumnDeclarationExpression(
    name = name, sqlType = sqlType, notNull = notNull, default = default, size = size, autoIncrement = autoIncrement
)

/**
 * A table constraint.
 */
abstract class TableConstraintExpression : SqlExpression()

/**
 * A foreign key constraint.
 *
 * @property otherTable the table the given columns reference.
 * @property correspondence a map of columns from this table to columns in the [otherTable]
 * @property onUpdate the action to perform when a key is updated in the target row
 * @property onDelete the action to perform when the target row is deleted
 */
data class ForeignKeyTableConstraintExpression(
    val otherTable: TableReferenceExpression,
    val correspondence: Map<ColumnReferenceExpression, ColumnReferenceExpression>,
    val onUpdate: ForeignKeyConstraint.OnModification = ForeignKeyConstraint.OnModification.CASCADE,
    val onDelete: ForeignKeyConstraint.OnModification = ForeignKeyConstraint.OnModification.ERROR,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : TableConstraintExpression()

/**
 * An arbitrary SQL expression to ensure when modifications are made.
 *
 * @property condition The condition to check.
 */
data class CheckTableConstraintExpression(
    val condition: ScalarExpression<Boolean>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : TableConstraintExpression()

/**
 * A constraint that ensures the given columns in [across] are unique together across all rows of the table.
 *
 * @property across the columns to ensure are unique together.
 */
data class UniqueTableConstraintExpression(
    val across: List<ColumnReferenceExpression>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : TableConstraintExpression()

/**
 * A required constraint that represents which columns are the primary key of the table.
 *
 * @property across the columns to use as the key.
 */
data class PrimaryKeyTableConstraintExpression(
    val across: List<ColumnReferenceExpression>,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = emptyMap()
) : TableConstraintExpression()

/**
 * A reference to a table that cannot be aliased.
 */
data class TableReferenceExpression(
    val name: String,
    val catalog: String? = null,
    val schema: String? = null,
    override val isLeafNode: Boolean = true,
    override val extraProperties: Map<String, Any> = emptyMap(),
) : SqlExpression()

/**
 * Makes a [TableReferenceExpression] from a table.
 */
fun BaseTable<*>.asReferenceExpression(): TableReferenceExpression =
    TableReferenceExpression(name = tableName, catalog = catalog, schema = schema)

/**
 * A reference to a column with an implied table.
 */
data class ColumnReferenceExpression(
    val name: String,
    override val isLeafNode: Boolean = true,
    override val extraProperties: Map<String, Any> = emptyMap(),
) : SqlExpression()

/**
 * Makes a [ColumnReferenceExpression] from a column.
 */
fun Column<*>.asReferenceExpression(): ColumnReferenceExpression = ColumnReferenceExpression(name)