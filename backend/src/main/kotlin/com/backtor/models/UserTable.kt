package com.backtor.models

import org.jetbrains.exposed.sql.Table
import java.util.UUID
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.ReferenceOption



object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 100).uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()  // <- nullable
    val streak = integer("streak").default(0)
    val lastActiveDate = datetime("last_active_date")
    val createdAt = datetime("created_at")
    val experienceTotal = integer("experience_total").default(0)
    val profileImageUrl = varchar("profile_image_url", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
object Followers : Table("followers") {
    val userId = integer("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val followerId = integer("follower_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val followedAt = datetime("followed_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(userId, followerId)
}
