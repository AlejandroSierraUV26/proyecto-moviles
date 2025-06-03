package com.example.proyecto.ui.modules

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultadosViewModel : ViewModel() {

    // Estado del resultado del examen (porcentaje, correctas, total)
    private val _examResult = MutableStateFlow<ExamResult?>(null)
    val examResult: StateFlow<ExamResult?> = _examResult.asStateFlow()

    // Nuevo: Estado del feedback detallado por pregunta
    private val _examFeedback = MutableStateFlow<ExamFeedbackResult?>(null)
    val examFeedback: StateFlow<ExamFeedbackResult?> = _examFeedback.asStateFlow()

    // Estado de carga (loading, error, etc.)
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    // Estado de las respuestas del usuario
    private val _userAnswers = MutableStateFlow<List<AnswerSubmission>>(emptyList())
    val userAnswers: StateFlow<List<AnswerSubmission>> = _userAnswers.asStateFlow()

    // Clase sellada para representar el estado de carga
    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    // Establecer las respuestas del usuario
    fun setUserAnswers(answers: List<AnswerSubmission>) {
        _userAnswers.value = answers
        Log.d("ResultadosViewModel", "User answers set: $answers")
    }

    // Enviar el examen y recibir feedback
    fun submitExam(examId: Int, answers: List<AnswerSubmission>) {
        Log.d("ResultadosViewModel", "Enviando examen con ID: $examId y respuestas: $answers")

        if (answers.isEmpty()) return

        _loadingState.value = LoadingState.Loading

        viewModelScope.launch {
            try {
                val submission = ExamSubmission(examId = examId, answers = answers)
                val response = RetrofitClient.apiService.evaluateExam(submission)

                Log.d("ResultadosViewModel", "Respuesta recibida: ${response.body()}")
                Log.d("ResultadosViewModel", "Código HTTP: ${response.code()}")
                Log.d("ResultadosViewModel", "Headers: ${response.headers()}")

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        _examFeedback.value = result // Guardamos el feedback completo
                        _examResult.value = mapFeedbackToResult(result) // Derivamos el resultado general
                        _loadingState.value = LoadingState.Idle
                    } else {
                        _loadingState.value = LoadingState.Error("Respuesta del servidor vacía.")
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error al evaluar el examen."
                    Log.d("ResultadosViewModel", "Respuesta cruda: $errorMsg")
                    _loadingState.value = LoadingState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    // Mapear el feedback a un resumen para mostrarlo más fácil
    private fun mapFeedbackToResult(feedback: ExamFeedbackResult): ExamResult {
        val percentage = (feedback.correct.toFloat() / feedback.total) * 100
        return ExamResult(
            correctAnswers = feedback.correct,
            totalQuestions = feedback.total,
            percentage = percentage.toInt()
        )
    }
}
