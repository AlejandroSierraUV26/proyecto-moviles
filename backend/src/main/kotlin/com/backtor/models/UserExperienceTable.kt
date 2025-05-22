package com.backtor.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table.PrimaryKey

object UserExperienceTable : Table("user_experience") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val experiencePoints = integer("experience_points")
    val collectedAt = datetime("collected_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
