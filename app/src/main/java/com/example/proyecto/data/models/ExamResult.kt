package com.example.proyecto.data.models

import android.health.connect.datatypes.units.Percentage
import kotlinx.serialization.Serializable

@Serializable
data class ExamResult(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val percentage: Int
)
