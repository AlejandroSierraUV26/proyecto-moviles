package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticSubmission(
    val courseId: Int,
    val maxLevel: Int, // 1, 2 or 3
    val answers: Map<Int, String> // questionId -> selectedAnswer
)

@Serializable
data class DiagnosticQuestion(
    val id: Int,
    val examId: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val difficultyLevel: Int
)

@Serializable
data class DiagnosticResult(
    val levelTested: Int,
    val passed: Boolean,
    val score: Double,
    val startingSection: String?,
    val message: String
)

@Serializable
data class DiagnosticFeedback(
    val results: List<DiagnosticResult>,
    val overallResult: String,
    val recommendedStartingSection: String
)