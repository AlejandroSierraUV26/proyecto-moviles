package com.backtor.models

import org.jetbrains.exposed.sql.Table
object QuestionTable : Table("questions") {
    val id = integer("id").autoIncrement()
    val examId = integer("exam_id").references(ExamTable.id) // FK a examen
    val questionText = text("question_text")
    val options = text("options")
    val correctAnswer = text("correct_answer")
    val feedback = text("feedback") // NUEVO
    override val primaryKey = PrimaryKey(id)
}
