package com.backtor.models

import org.jetbrains.exposed.sql.Table

object CategoryTable : Table("categories") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 100)
    
    override val primaryKey = PrimaryKey(id)
}