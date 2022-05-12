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

import org.ktorm.database.Database
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

fun BaseTable<*>.create(): CreateTableExpression {
    val tableConstraints: MutableMap<String, TableConstraintExpression> = mutableMapOf()
    tableConstraints["${catalog}_${schema}_${tableName}_pk"] =
        PrimaryKeyTableConstraintExpression(across = primaryKeys.map(Column<*>::asReferenceExpression))
    for ((name, constraint) in data.constraints) {
        tableConstraints[name] = constraint.asExpression()
    }
    return CreateTableExpression(
        name = asReferenceExpression(),
        columns = columns.map(Column<*>::asDeclarationExpression),
        constraints = tableConstraints
    )
}

fun BaseTable<*>.drop(): DropTableExpression = DropTableExpression(asReferenceExpression())

fun Column<*>.add(): AlterTableAddColumnExpression = AlterTableAddColumnExpression(
    table.asReferenceExpression(),
    asDeclarationExpression()
)

fun Column<*>.modify(): AlterTableModifyColumnExpression = AlterTableModifyColumnExpression(
    table.asReferenceExpression(),
    asReferenceExpression(),
    sqlType,
    size,
    notNull
)

fun Column<*>.drop(): AlterTableDropColumnExpression = AlterTableDropColumnExpression(
    table.asReferenceExpression(),
    asReferenceExpression()
)

infix fun Database.create(table: BaseTable<*>) = executeUpdate(table.create())

infix fun Database.drop(table: BaseTable<*>) = executeUpdate(table.drop())

infix fun Database.add(column: Column<*>) = executeUpdate(column.add())

infix fun Database.modify(column: Column<*>) = executeUpdate(column.modify())

infix fun Database.drop(column: Column<*>) = executeUpdate(column.drop())
