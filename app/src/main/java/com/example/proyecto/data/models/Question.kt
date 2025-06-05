package com.example.proyecto.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    @SerialName("id") val id: Int,
    @SerialName("examId") val examId: Int,
    @SerialName("questionText") val questionText: String,
    @SerialName("options") val options: List<String>,
    @SerialName("correctAnswer") val correctAnswer: String,
    @SerialName("feedback") val feedback: String // Cambiado a no nullable
)
@Serializable
data class QuestionRequest(
    @SerialName("sectionId") val sectionId: Int,
    @SerialName("difficultyLevel") val difficultyLevel: Int,
    @SerialName("questionText") val questionText: String,
    @SerialName("options") val options: List<String>,
    @SerialName("correctAnswer") val correctAnswer: String,
    @SerialName("feedback") val feedback: String // Cambiado a no nullable
)

// En models/ExamSubmission.kt
@Serializable
data class ExamSubmission(
    @SerialName("examId") val examId: Int,
    @SerialName("answers") val answers: List<AnswerSubmission>
)

@Serializable
data class AnswerSubmission(
    @SerialName("questionId") val questionId: Int,
    @SerialName("selectedAnswer") val selectedAnswer: String
)

// En models/ExamFeedbackResult.kt
@Serializable
data class ExamFeedbackResult(
    @SerialName("feedbackList") val feedbackList: List<AnswerFeedback>,
    @SerialName("correctAnswers") val correctAnswers: Int,
    @SerialName("totalQuestions") val totalQuestions: Int,
    @SerialName("percentage") val percentage: Int
)