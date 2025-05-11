package com.backtor.models

import org.jetbrains.exposed.sql.Table

object QuestionTable : Table("questions") {
    val id = integer("id").autoIncrement()
    val courseId = integer("courseId").references(CourseTable.id)
    val difficultyLevel = integer("difficultyLevel")
    val questionText = text("questionText")
    val options = text("options") // JSON array
    val correctAnswer = text("correctAnswer")
    
    override val primaryKey = PrimaryKey(id)
}