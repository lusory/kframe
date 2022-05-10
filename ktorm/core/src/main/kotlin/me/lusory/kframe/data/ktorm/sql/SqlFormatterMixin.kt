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
import org.ktorm.database.DialectFeatureNotSupportedException
import org.ktorm.database.SqlDialect
import org.ktorm.expression.*
import java.util.*

interface SqlFormatterMixin {
    val superVisitor: (SqlExpression) -> SqlExpression

    fun write0(value: String)

    fun writeKeyword0(keyword: String)

    fun <T : Any> visitScalar0(expr: ScalarExpression<T>): ScalarExpression<T>

    fun visitSelect0(expr: SelectExpression): SelectExpression

    fun visitTable0(expr: TableExpression): TableExpression

    fun quoteString(str: String): String

    fun visit0(expr: SqlExpression): SqlExpression = when (expr) {
        is CreateSchemaExpression -> visitCreateSchema(expr)
        is DropSchemaExpression -> visitDropSchema(expr)
        is TableReferenceExpression -> visitTableReference(expr)
        is CreateTableExpression -> visitCreateTable(expr)
        is DropTableExpression -> visitDropTable(expr)
        is TruncateTableExpression -> visitTruncateTable(expr)
        is AlterTableAddExpression -> visitAlterTableAdd(expr)
        is AlterTableDropColumnExpression -> visitAlterTableDropColumn(expr)
        is AlterTableModifyColumnExpression -> visitAlterTableModifyColumn(expr)
        is AlterTableSetDefaultExpression -> visitAlterTableSetDefault(expr)
        is AlterTableDropDefaultExpression -> visitAlterTableDropDefault(expr)
        is AlterTableAddConstraintExpression -> visitAlterTableAddConstraint(expr)
        is AlterTableDropConstraintExpression -> visitAlterTableDropConstraint(expr)
        is CreateIndexExpression -> visitCreateIndex(expr)
        is DropIndexExpression -> visitDropIndex(expr)
        is CreateViewExpression -> visitCreateView(expr)
        is DropViewExpression -> visitDropView(expr)
        is ColumnDeclarationExpression<*> -> visitColumnDeclaration(expr)
        is TableConstraintExpression -> visitTableConstraint(expr)
        else -> superVisitor(expr)
    }

    fun visitTableConstraint(expr: TableConstraintExpression): TableConstraintExpression = when (expr) {
        is ForeignKeyTableConstraintExpression -> visitForeignKeyTableConstraint(expr)
        is CheckTableConstraintExpression -> visitCheckTableConstraint(expr)
        is UniqueTableConstraintExpression -> visitUniqueTableConstraint(expr)
        is PrimaryKeyTableConstraintExpression -> visitPrimaryKeyTableConstraint(expr)
        else -> superVisitor(expr) as TableConstraintExpression
    }

    fun visitCreateSchema(expr: CreateSchemaExpression): CreateSchemaExpression {
        writeKeyword0("create schema ")
        if (expr.ifNotExists) {
            writeKeyword0("if not exists ")
        }
        write0(quoteString(expr.name))
        return expr
    }

    fun visitDropSchema(expr: DropSchemaExpression): DropSchemaExpression {
        writeKeyword0("drop schema ")
        write0(quoteString(expr.name))
        return expr
    }

    fun visitCreateTable(expr: CreateTableExpression): CreateTableExpression {
        writeKeyword0("create table ")
        if (expr.ifNotExists) {
            writeKeyword0("if not exists ")
        }
        visitTableReference(expr.name)

        write0("(")
        var first = true
        for (col in expr.columns) {
            if (first) first = false
            else write0(", ")
            visitColumnDeclaration(col)
        }
        for (constraint in expr.constraints.entries) {
            writeKeyword0(", constraint ")
            write0(constraint.key)
            write0(" ")
            visit0(constraint.value)
        }
        write0(") ")

        return expr
    }

    fun visitDropTable(expr: DropTableExpression): DropTableExpression {
        writeKeyword0("drop table ")
        visitTableReference(expr.table)
        return expr
    }

    fun visitTruncateTable(expr: TruncateTableExpression): TruncateTableExpression {
        writeKeyword0("truncate table ")
        visitTableReference(expr.table)
        return expr
    }

    fun visitAlterTableAdd(expr: AlterTableAddExpression): AlterTableAddExpression {
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        writeKeyword0(" add ")
        visitColumnDeclaration(expr.column)
        return expr
    }

    fun visitAlterTableDropColumn(expr: AlterTableDropColumnExpression): AlterTableDropColumnExpression {
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        writeKeyword0(" drop column ")
        write0(quoteString(expr.column.name))
        return expr
    }

    fun visitAlterTableModifyColumn(expr: AlterTableModifyColumnExpression): AlterTableModifyColumnExpression {
        // This syntax is basically completely specific to the database in question.
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        writeKeyword0(" alter column ")
        write0(quoteString(expr.column.name))
        write0(" ")
        writeKeyword0(expr.newType.typeName)
        if (expr.size != null) {
            write0("(")
            write0(expr.size.toString())
            write0(")")
        }
        if (expr.notNull) {
            writeKeyword0(" not null")
        }
        return expr
    }

    fun visitAlterTableAddConstraint(expr: AlterTableAddConstraintExpression): AlterTableAddConstraintExpression {
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        writeKeyword0(" add constraint ")
        write0(expr.constraintName)
        write0(" ")
        visit0(expr.tableConstraint)
        return expr
    }

    fun visitAlterTableDropConstraint(expr: AlterTableDropConstraintExpression): AlterTableDropConstraintExpression {
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        // MySQL has a custom syntax, unfortunately, where instead of using 'constraint', you use the type of the constraint
        writeKeyword0(" drop constraint ")
        write0(expr.constraintName)
        return expr
    }

    fun visitCreateIndex(expr: CreateIndexExpression): CreateIndexExpression {
        writeKeyword0("create index ")
        write0(quoteString(expr.name))
        writeKeyword0(" on ")
        visitTableReference(expr.on)
        writeKeyword0(" (")
        var first = true
        for (col in expr.columns) {
            if (first) first = false
            else write0(", ")
            write0(quoteString(col.name))
        }
        write0(")")
        return expr
    }

    fun visitDropIndex(expr: DropIndexExpression): DropIndexExpression {
        writeKeyword0("drop index ")
        write0(quoteString(expr.name))
        writeKeyword0(" on ")
        visitTableReference(expr.on)
        return expr
    }

    fun visitCreateView(expr: CreateViewExpression): CreateViewExpression {
        if (expr.orReplace) {
            writeKeyword0("create or replace view ")
        } else {
            writeKeyword0("create view ")
        }
        visitTableReference(expr.name)
        writeKeyword0(" as ")
        visitSelect0(expr.query)
        return expr
    }

    fun visitDropView(expr: DropViewExpression): DropViewExpression {
        writeKeyword0("drop view ")
        visitTableReference(expr.name)
        return expr
    }

    fun visitColumnDeclaration(expr: ColumnDeclarationExpression<*>): ColumnDeclarationExpression<*> {
        write0(quoteString(expr.name))
        write0(" ")
        writeKeyword0(expr.sqlType.typeName)
        if (expr.size != null) {
            write0("(")
            write0(expr.size.toString())
            write0(")")
        }
        if (expr.notNull) {
            writeKeyword0(" not null")
        }
        if (expr.default != null) {
            writeKeyword0(" default ")
            visitScalar0(expr.default)
        }
        if (expr.autoIncrement) {
            throw RuntimeException("Auto increment is not supported by the general formatter.")
        }
        return expr
    }

    fun visitForeignKeyTableConstraint(expr: ForeignKeyTableConstraintExpression): ForeignKeyTableConstraintExpression {
        writeKeyword0("foreign key (")
        val orderedEntries = expr.correspondence.entries.toList()
        var first = true
        for (col in orderedEntries) {
            if (first) first = false
            else write0(", ")
            write0(quoteString(col.key.name))
        }
        writeKeyword0(") references ")
        visitTableReference(expr.otherTable)
        write0("(")
        first = true
        for (col in orderedEntries) {
            if (first) first = false
            else write0(", ")
            write0(quoteString(col.value.name))
        }
        // might not be supported everywhere
        writeKeyword0(") on update ")
        when (expr.onUpdate) {
            ForeignKeyConstraint.OnModification.ERROR -> writeKeyword0("restrict")
            ForeignKeyConstraint.OnModification.CASCADE -> writeKeyword0("cascade")
            ForeignKeyConstraint.OnModification.SET_NULL -> writeKeyword0("set null")
            ForeignKeyConstraint.OnModification.SET_DEFAULT -> writeKeyword0("set default")
        }
        writeKeyword0(" on delete ")
        when (expr.onUpdate) {
            ForeignKeyConstraint.OnModification.ERROR -> writeKeyword0("restrict")
            ForeignKeyConstraint.OnModification.CASCADE -> writeKeyword0("cascade")
            ForeignKeyConstraint.OnModification.SET_NULL -> writeKeyword0("set null")
            ForeignKeyConstraint.OnModification.SET_DEFAULT -> writeKeyword0("set default")
        }
        return expr
    }

    fun visitCheckTableConstraint(expr: CheckTableConstraintExpression): CheckTableConstraintExpression {
        writeKeyword0("check (")
        visitScalar0(expr.condition)
        write0(")")
        return expr
    }

    fun visitUniqueTableConstraint(expr: UniqueTableConstraintExpression): UniqueTableConstraintExpression {
        writeKeyword0("unique (")
        var first = true
        for (col in expr.across) {
            if (first) first = false
            else write0(", ")
            write0(quoteString(col.name))
        }
        write0(")")
        return expr
    }

    fun visitPrimaryKeyTableConstraint(expr: PrimaryKeyTableConstraintExpression): PrimaryKeyTableConstraintExpression {
        writeKeyword0("primary key (")
        var first = true
        for (col in expr.across) {
            if (first) first = false
            else write0(", ")
            write0(quoteString(col.name))
        }
        write0(")")
        return expr
    }

    fun visitTableReference(expr: TableReferenceExpression): TableExpression = visitTable0(
        TableExpression(
            name = expr.name,
            tableAlias = null,
            catalog = expr.catalog,
            schema = expr.schema,
            isLeafNode = expr.isLeafNode,
            extraProperties = expr.extraProperties
        )
    )

    fun visitAlterTableSetDefault(expr: AlterTableSetDefaultExpression): AlterTableSetDefaultExpression {
        // This is unique across every database, H2 and MS Access use this though - SQLite has zero support
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        writeKeyword0(" alter column ")
        write0(quoteString(expr.column.name))
        writeKeyword0(" set default ")
        visitScalar0(expr.default)
        return expr
    }

    fun visitAlterTableDropDefault(expr: AlterTableDropDefaultExpression): AlterTableDropDefaultExpression {
        // Closer to universal, only mysql has this problem - SQLite has zero support
        writeKeyword0("alter table ")
        visitTableReference(expr.table)
        writeKeyword0(" alter column ")
        write0(quoteString(expr.column.name))
        writeKeyword0(" drop default")
        return expr
    }
}

internal class SqlFormatterMixinImpl(database: Database, beautifySql: Boolean, indentSize: Int) : SqlFormatter(database, beautifySql, indentSize), SqlFormatterMixin {
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

    override fun writePagination(expr: QueryExpression) {
        throw DialectFeatureNotSupportedException("Pagination is not supported in Standard SQL.")
    }

    override fun visit(expr: SqlExpression): SqlExpression = visit0(expr)
}

interface MixinDialect : SqlDialect {
    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter =
        SqlFormatterMixinImpl(database, beautifySql, indentSize)
}

fun detectMixinDialectImplementation(): MixinDialect {
    val dialects: List<MixinDialect> = ServiceLoader.load(MixinDialect::class.java).toList()
    return when (dialects.size) {
        0 -> object : MixinDialect {}
        1 -> dialects[0]
        else -> error(
            "More than one mixin dialect implementation found in the classpath, please choose one manually: $dialects"
        )
    }
}
