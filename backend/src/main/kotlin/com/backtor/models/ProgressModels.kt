package com.backtor.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable(with = LocalDateTimeSerializer::class)
data class LocalDateTimeWrapper(val value: LocalDateTime)

@Serializable
data class CourseProgressResponse(
    val courseProgress: Int,
    val sections: List<SectionProgress>
)

@Serializable
data class SectionProgress(
    val sectionId: Int,
    val title: String,
    val exams: List<ExamProgress>,
    val completedExams: Int,
    val totalExams: Int
)

@Serializable
data class ExamProgress(
    val examId: Int,
    val title: String,
    val completed: Boolean,
    val bestScore: Int,
    val lastAttemptDate: LocalDateTimeWrapper?
)

@Serializable
data class UserCourseWithProgress(
    val id: Int,
    val title: String,
    val description: String,
    val progress: Int,
    val sections: Int,
    val completedSections: Int
)