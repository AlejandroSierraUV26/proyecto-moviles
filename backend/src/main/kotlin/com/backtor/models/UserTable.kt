package com.backtor.models

import org.jetbrains.exposed.sql.Table
import java.util.UUID
import org.jetbrains.exposed.sql.javatime.datetime

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 255)
    val description = text("description")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(id)
}
