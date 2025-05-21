package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Int,
    val examId: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val feedback: String // NUEVO
)

@Serializable
data class QuestionRequest(
    val sectionId: Int,
    val difficultyLevel: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val feedback: String // NUEVO
)



