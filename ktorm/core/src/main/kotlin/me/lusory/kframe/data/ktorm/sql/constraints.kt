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

import org.ktorm.expression.ScalarExpression
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column

/**
 * Represents a constraint on an SQL table.
 */
abstract class Constraint {
    /**
     * Returns a [TableConstraintExpression] representing the definition of this constraint.
     */
    abstract fun asExpression(): TableConstraintExpression
}

/**
 * A unique constraint, which ensures that every row has a different set of values for the given columns.
 */
data class UniqueConstraint(val across: List<Column<*>>) : Constraint() {
    override fun equals(other: Any?): Boolean = other is UniqueConstraint && this.across.zip(other.across) { a, b -> a.name == b.name }.all { it }

    override fun hashCode(): Int = across.hashCode()

    override fun asExpression(): UniqueTableConstraintExpression = UniqueTableConstraintExpression(
        across = across.map(Column<*>::asReferenceExpression)
    )
}

/**
 * A check constraint, which ensures that inserted or updated rows adhere to the given [condition].
 */
data class CheckConstraint(val condition: ScalarExpression<Boolean>) : Constraint() {
    override fun equals(other: Any?): Boolean = other is CheckConstraint && this.condition == other.condition

    override fun hashCode(): Int = condition.hashCode()

    override fun asExpression(): CheckTableConstraintExpression = CheckTableConstraintExpression(condition)
}

/**
 * A foreign key constraint, which ensures that the columns in [correspondence] map to the target table.
 * If the target row is modified, [onUpdate] is triggered.
 * If the target row is deleted, [onDelete] is triggered.
 */
data class ForeignKeyConstraint(
    val to: BaseTable<*>,
    val correspondence: Map<Column<*>, Column<*>>,
    val onUpdate: OnModification = OnModification.CASCADE,
    val onDelete: OnModification = OnModification.ERROR,
) : Constraint() {

    /**
     * Represents the different ways of handling a target row modification.
     */
    enum class OnModification {
        /**
         * Throw an error, do not proceed.
         */
        ERROR,

        /**
         * Corresponding rows are updated or deleted in the referencing table when that row is updated or deleted in the parent table.
         */
        CASCADE,

        /**
         * References to the target row are set to null.
         */
        SET_NULL,

        /**
         * References to the target row are set to the default value.
         */
        SET_DEFAULT
    }

    override fun equals(other: Any?): Boolean =
        other is ForeignKeyConstraint && this.correspondence.entries.zip(other.correspondence.entries) { a, b -> a.key.name == b.key.name && a.value.name == b.value.name }.all { it }

    override fun hashCode(): Int {
        var result = to.hashCode()
        result = 31 * result + correspondence.hashCode()
        result = 31 * result + onUpdate.hashCode()
        result = 31 * result + onDelete.hashCode()
        return result
    }

    override fun asExpression(): ForeignKeyTableConstraintExpression = ForeignKeyTableConstraintExpression(
        otherTable = to.asReferenceExpression(),
        correspondence = correspondence.entries.associate {
            it.key.asReferenceExpression() to it.value.asReferenceExpression()
        },
        onUpdate = onUpdate,
        onDelete = onDelete,
    )
}