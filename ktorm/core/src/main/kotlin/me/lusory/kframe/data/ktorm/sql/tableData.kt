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
import kotlin.reflect.KClass

internal val TABLE_DATA: MutableMap<KClass<*>, TableData<*>> = mutableMapOf()

data class TableData<T : Any>(
    /**
     * A reference to a [BaseTable].
     */
    val table: BaseTable<T>,

    /**
     * Stores which columns have a "not null" restriction.
     */
    val columnNotNull: MutableSet<String> = mutableSetOf(),

    /**
     * Stores which columns have auto increment enabled.
     */
    val columnAutoIncrement: MutableSet<String> = mutableSetOf(),

    /**
     * Stores the sizes of each column for SQL types like VARCHAR.
     */
    val columnSize: MutableMap<String, Int> = mutableMapOf(),

    /**
     * Stores the default value expression for each column.
     */
    val columnDefault: MutableMap<String, ScalarExpression<*>> = mutableMapOf(),

    /**
     * Stores the related constraints by name.
     */
    val constraints: MutableMap<String, Constraint> = mutableMapOf()
)

@Suppress("UNCHECKED_CAST")
val <T : Any> BaseTable<T>.data: TableData<T>
    get() = TABLE_DATA.getOrPut(javaClass.kotlin) { TableData(this) } as TableData<T>