package com.example.proyecto.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerFeedback(
    val questionId: Int,
    val questionText: String,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)



