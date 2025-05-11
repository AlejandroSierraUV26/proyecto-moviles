package com.backtor.models

import org.jetbrains.exposed.sql.Table

object CourseTable : Table("courses") {
    val id = integer("id").autoIncrement()
    val categoryId = integer("category_id").references(CategoryTable.id)
    val title = varchar("title", 100)
    val description = text("description")
    
    override val primaryKey = PrimaryKey(id)
}