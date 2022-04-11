package me.lusory.kframe.data.ktorm

import me.lusory.kframe.inject.Component
import me.lusory.kframe.inject.Exact
import org.ktorm.database.Database
import java.util.*

@Component(name = "ktormDatabase")
fun ktormDatabase(@Exact(name = "properties") props: Properties): Database = Database.connect(
    props["ktorm.url"] as? String ?: throw RuntimeException("ktorm.url property not provided"),
    driver = props["ktorm.driver"] as? String
)