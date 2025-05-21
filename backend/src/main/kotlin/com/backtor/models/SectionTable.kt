package com.backtor.models

import org.jetbrains.exposed.sql.Table


object SectionTable : Table("sections") {
    val id = integer("id").autoIncrement()
    val courseId = integer("course_id").references(CourseTable.id)

    val title = varchar("title", 100)
    val difficultyLevel = integer("difficulty_level")

    override val primaryKey = PrimaryKey(id)
}