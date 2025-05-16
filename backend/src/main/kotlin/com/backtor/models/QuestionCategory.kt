package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Int,
    val courseId: Int,
    val difficultyLevel: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)
@Serializable
data class QuestionRequest(
    val courseId: Int,
    val difficultyLevel: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
) {
    init {
        require(options.size >= 2) { "Debe haber al menos 2 opciones" }
        require(correctAnswer in options) { "La respuesta correcta debe estar en las opciones" }
    }
}

