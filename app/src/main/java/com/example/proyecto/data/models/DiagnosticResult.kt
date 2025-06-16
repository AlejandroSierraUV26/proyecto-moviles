package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticResult(
    val levelTested: Int,
    val passed: Boolean,
    val score: Double,
    val startingSection: String,
    val message: String,
)