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

package me.lusory.kframe.data.ktorm.mysql

import me.lusory.kframe.data.ktorm.sql.*
import org.ktorm.database.Database
import org.ktorm.expression.*
import org.ktorm.support.mysql.MySqlDialect
import org.ktorm.support.mysql.MySqlFormatter

open class MySqlFormatterMixinImpl(
    database: Database, beautifySql: Boolean, indentSize: Int
) : MySqlFormatter(database, beautifySql, indentSize), SqlFormatterMixin {
    override val superVisitor: (SqlExpression) -> SqlExpression
        get() = { super.visit(it) }

    override fun write0(value: String) {
        write(value)
    }

    override fun writeKeyword0(keyword: String) {
        writeKeyword(keyword)
    }

    override fun <T : Any> visitScalar0(expr: ScalarExpression<T>): ScalarExpression<T> = visitScalar(expr)

    override fun visitSelect0(expr: SelectExpression): SelectExpression = visitSelect(expr)

    override fun visitTable0(expr: TableExpression): TableExpression = visitTable(expr)

    override fun quoteString(str: String): String = str.quoted

    override fun visit(expr: SqlExpression): SqlExpression = visit0(expr)

    override fun visitAlterTableAdd(expr: AlterTableAddExpression): AlterTableAddExpression {
        writeKeyword("alter table ")
        visitTableReference(expr.table)
        writeKeyword(" add column ")
        visitColumnDeclaration(expr.column)
        return expr
    }

    override fun visitAlterTableModifyColumn(expr: AlterTableModifyColumnExpression): AlterTableModifyColumnExpression {
        writeKeyword("alter table ")
        visitTableReference(expr.table)
        writeKeyword(" modify column ")
        write(expr.column.name.quoted)
        write(" ")
        writeKeyword(expr.newType.typeName)
        if (expr.size != null) {
            write("(")
            write(expr.size.toString())
            write(")")
        }
        if (expr.notNull) {
            writeKeyword(" not null")
        }
        return expr
    }

    override fun visitAlterTableDropConstraint(expr: AlterTableDropConstraintExpression): AlterTableDropConstraintExpression {
        writeKeyword("alter table ")
        visitTableReference(expr.table)
        writeKeyword(" drop ")
        when (expr.tableConstraint) {
            is PrimaryKeyTableConstraintExpression -> writeKeyword("primary key")
            is ForeignKeyTableConstraintExpression -> {
                writeKeyword("foreign key ")
                write(expr.constraintName)
            }
            is UniqueTableConstraintExpression -> {
                writeKeyword("index ")
                write(expr.constraintName)
            }
            is CheckTableConstraintExpression -> {
                writeKeyword("check ")
                write(expr.constraintName)
            }
        }
        return expr
    }


    override fun visitColumnDeclaration(expr: ColumnDeclarationExpression<*>): ColumnDeclarationExpression<*> {
        write(expr.name.quoted)
        write(" ")
        writeKeyword(expr.sqlType.typeName)
        if (expr.size != null) {
            write("(")
            write(expr.size.toString())
            write(")")
        }
        if (expr.notNull) {
            writeKeyword(" not null")
        }
        if (expr.default != null) {
            writeKeyword(" default ")
            visitScalar(expr.default!!)
        }
        if (expr.autoIncrement) {
            writeKeyword(" auto_increment")
        }
        return expr
    }
}

open class MySqlMixinDialect : MySqlDialect(), MixinDialect {
    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter =
        MySqlFormatterMixinImpl(database, beautifySql, indentSize)
}