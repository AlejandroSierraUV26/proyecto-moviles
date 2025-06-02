package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ExamResult(
    val examId: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val score: Float,
    val feedback: List<AnswerFeedback>
)
