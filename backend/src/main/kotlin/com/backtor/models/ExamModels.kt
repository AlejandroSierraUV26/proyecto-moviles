package com.backtor.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Exam(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String?,
    @SerialName("section_id")
    val sectionId: Int,
    @SerialName("difficulty_level")
    val difficultyLevel: Int
)



@Serializable
data class ExamRequest(
    val id: Int,
    val title: String,
    val description: String?,
    val sectionId: Int,
    val difficultyLevel: Int
)



@Serializable
data class QuestionAnswer(
    val questionId: Int,
    val selectedAnswer: String
)

@Serializable
data class ExamSubmission(
    val examId: Int,
    val answers: List<QuestionAnswer>
)


@Serializable
data class ExamResult(
    val correctCount: Int,
    val totalQuestions: Int,
    val percentage: Int
)

@Serializable
data class AnswerFeedback(
    val questionId: Int,
    val questionText: String,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)

@Serializable
data class ResourceRecommendation(
    val title: String,
    val url: String,
    val source: String
)

@Serializable
data class ExamFeedbackResult(
    val feedbackList: List<AnswerFeedback>,
    val correct: Int,
    val total: Int,
    val percentage: Int,
    val recommendations: List<ResourceRecommendation> = emptyList(),
    val motivationalMessage: String = ""
) {
    val correctAnswers: Int get() = correct
    val totalAnswers: Int get() = total
}
