package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticResult(
    val startingSection: String,
    val message: String,
    val correctAnswers: Int,
    val totalQuestions: Int
)