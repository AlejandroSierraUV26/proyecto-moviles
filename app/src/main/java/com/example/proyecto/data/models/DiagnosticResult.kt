package com.example.proyecto.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticFeedback(
    val results: List<DiagnosticResult>,
    val overallResult: String,
    val recommendedStartingSection: String
)

@Serializable
data class DiagnosticResult(
    val levelTested: Int,
    val passed: Boolean,
    val score: Double,
    val startingSection: String,
    val message: String
)

@Serializable
data class ProgressResponse(
    val courseId: Int,
    val courseTitle: String,
    val overallProgress: Int,
    val sections: List<SectionProgress>
)

@Serializable
data class SectionProgress(
    val sectionId: Int,
    val sectionTitle: String,
    val progress: Int,
    val exams: List<ExamProgress>
)
@Serializable
data class ExamProgress(
    val examId: Int,
    val examTitle: String,
    val completed: Boolean,
    val score: Int
)