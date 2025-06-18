package com.backtor.models

import org.jetbrains.exposed.sql.Table

object UserCoursesTable : Table("user_courses") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UserTable.id)
    val courseId = integer("course_id").references(CourseTable.id)
    val progressPercentage = integer("progress_percentage").default(0)
    val completed = bool("completed").default(false)

    override val primaryKey = PrimaryKey(id)
}
