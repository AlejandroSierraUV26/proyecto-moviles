package com.backtor.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UserExamProgressTable : Table("user_exam_progress") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UserTable.id)
    val examId = integer("exam_id").references(ExamTable.id)
    val questionsAnswered = integer("questions_answered").default(0)
    val questionsCorrect = integer("questions_correct").default(0)
    val completed = bool("completed").default(false)
    val lastAttemptDate = datetime("last_attempt_date").nullable()
    val bestScore = integer("best_score").default(0)

    override val primaryKey = PrimaryKey(id)
    init {
        uniqueIndex(userId, examId)
    }
}