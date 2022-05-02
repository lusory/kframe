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

package me.lusory.kframe.data.ktorm.test

import me.lusory.kframe.data.ktorm.database
import me.lusory.kframe.data.ktorm.sql.*
import org.junit.jupiter.api.BeforeAll
import org.ktorm.database.Database
import org.ktorm.dsl.insert
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate
import java.util.*
import kotlin.test.Test

class SchemaTest {
    companion object {
        private lateinit var database: Database

        @BeforeAll
        @JvmStatic
        fun init() {
            database = database {
                jdbcUrl = "jdbc:h2:mem:ktorm;DB_CLOSE_DELAY=-1"
            }
        }
    }

    @Test
    fun create() {
        database.create(Departments)
        database.create(Employees)
        database.insert(Departments) {
            set(it.id, 0)
            set(it.name, "Test Department")
            set(it.location, "Some Location")
            set(it.mixedCase, "WhEeEeEe")
        }
        database.insert(Employees) {
            set(it.id, 0)
            set(it.name, "Bob Boberson")
            set(it.departmentId, 0)
        }
        database.drop(Employees)
        database.drop(Departments)
    }

    interface Department : Entity<Department> {
        companion object : Entity.Factory<Department>()

        val id: Int
        var name: String
        var location: String?
        var mixedCase: String?
    }

    interface Employee : Entity<Employee> {
        companion object : Entity.Factory<Employee>()

        var id: Int
        var name: String
        var job: String
        var manager: Employee?
        var hireDate: LocalDate
        var salary: Long
        var department: Department

        val upperName get() = name.uppercase(Locale.getDefault())
        fun upperName() = name.uppercase(Locale.getDefault())
    }

    interface Customer : Entity<Customer> {
        companion object : Entity.Factory<Customer>()

        var id: Int
        var name: String
        var email: String
        var phoneNumber: String
        var address: String
    }

    @Suppress("LeakingThis")
    open class Departments(alias: String?) : Table<Department>("t_department", alias) {
        companion object : Departments(null)

        override fun aliased(alias: String) = Departments(alias)

        val id = int("id").primaryKey().bindTo { it.id }
        val name = varchar("name").size(128).bindTo { it.name }
        val location =
            varchar("location").default("Unimportant").size(128).bindTo { it.location }
        val mixedCase = varchar("mixedCase").size(128).bindTo { it.mixedCase }
    }

    @Suppress("LeakingThis")
    open class Employees(alias: String?) : Table<Employee>("t_employee", alias) {
        companion object : Employees(null)

        override fun aliased(alias: String) = Employees(alias)

        val id = int("id").primaryKey().bindTo { it.id }
        val name = varchar("name").size(128).unique().bindTo { it.name }
        val job = varchar("job").size(128).default("Minion").bindTo { it.job }
        val managerId = int("manager_id").bindTo { it.manager?.id }
        val hireDate = date("hire_date").bindTo { it.hireDate }
        val salary = long("salary").bindTo { it.salary }
        val departmentId = int("department_id").foreignKey(Departments).references(Departments) { it.department }
        val department = departmentId.referenceTable as Departments
    }

    @Suppress("LeakingThis")
    open class Customers(alias: String?) : Table<Customer>("t_customer", alias, schema = "company") {
        companion object : Customers(null)

        override fun aliased(alias: String) = Customers(alias)

        val id = int("id").primaryKey().bindTo { it.id }
        val name = varchar("name").size(128).bindTo { it.name }
        val email = varchar("email").size(128).bindTo { it.email }
        val phoneNumber = varchar("phone_number").size(128).bindTo { it.phoneNumber }
        val address = varchar("address").size(512).bindTo { it.address }
    }

    val Database.departments get() = this.sequenceOf(Departments)
    val Database.employees get() = this.sequenceOf(Employees)
    val Database.customers get() = this.sequenceOf(Customers)
}