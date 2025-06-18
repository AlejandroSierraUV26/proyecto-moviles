package com.backtor.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object CourseTable : Table("courses") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 100)
    val description = text("description")
    val createdBy = integer("created_by")
    override val primaryKey = PrimaryKey(id)
}

object CoursePreviews : Table("course_previews") {
    val id = integer("id").autoIncrement()
    val topic = varchar("topic", length = 255)
    val jsonContent = text("json_content")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime) // Sin paréntesis
    val createdBy = integer("created_by").references(UserTable.id)
    val processed = bool("processed").default(false)
    val processedAt = datetime("processed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}