package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AnswerFeedback(
    val questionId: Int,
    val questionText: String,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val percentage: Int,
    val feedback: String // <-- Esta es la propiedad que debes usar
)
