package com.backtor.models

import org.jetbrains.exposed.sql.Table
import java.util.UUID
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.javatime.date




object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 100).uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val streak = integer("streak").default(0)
    val lastActiveDate = datetime("last_active_date")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
