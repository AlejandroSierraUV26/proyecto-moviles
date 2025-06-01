package com.backtor.models

import kotlinx.serialization.Serializable

data class Exam(
    val id: Int,
    val title: String,
    val description: String?,
    val sectionId: Int,
    val difficultyLevel: Int
)



@Serializable
data class ExamRequest(
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
data class ExamFeedbackResult(
    val feedbackList: List<AnswerFeedback>,
    val correct: Int,  // Asegúrate de que esta propiedad exista
    val total: Int,    // Asegúrate de que esta propiedad exista
    val percentage: Int
) {
    // Propiedades calculadas para compatibilidad
    val correctAnswers: Int get() = correct
    val totalAnswers: Int get() = total
}
