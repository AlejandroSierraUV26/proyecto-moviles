package com.backtor.models
import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticSubmission(
    val courseId: Int,
    val level: String,
    val answers: Map<Int, String> // questionId -> selectedAnswer
)
