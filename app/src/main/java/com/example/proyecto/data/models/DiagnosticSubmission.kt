package com.example.proyecto.data.models
import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticSubmission(
    val courseId: Int,
    val maxLevel: Int,
    val answers: Map<Int, String>
)

@Serializable
data class DiagnosticQuestion(
    val id: Int,
    val examId: Int,
    val questionText: String,
    val options: List<String>,
    val difficultyLevel: Int,
    val correctAnswer: String,
    val recommendedStartingSection: String
)

