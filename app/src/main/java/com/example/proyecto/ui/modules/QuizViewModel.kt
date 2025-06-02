package com.example.proyecto.ui.modules

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.AnswerFeedback
import com.example.proyecto.data.models.AnswerSubmission
import com.example.proyecto.data.models.ExamSubmission
import com.example.proyecto.data.models.ExamResult
import com.example.proyecto.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class QuizViewModel : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _examResult = MutableStateFlow<ExamResult?>(null)
    val examResult: StateFlow<ExamResult?> = _examResult.asStateFlow()

    private val _userAnswers = MutableStateFlow<List<AnswerSubmission>>(emptyList())
    val userAnswers: StateFlow<List<AnswerSubmission>> = _userAnswers.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    fun updateAnswer(answer: AnswerSubmission) {
        val currentAnswers = _userAnswers.value.toMutableList()
        // Reemplaza o agrega la respuesta para la pregunta
        val index = currentAnswers.indexOfFirst { it.questionId == answer.questionId }
        if (index != -1) {
            currentAnswers[index] = answer
        } else {
            currentAnswers.add(answer)
        }
        _userAnswers.value = currentAnswers
    }

    fun setResultados(
        correctCount: Int,
        totalQuestions: Int,
        feedback: List<AnswerFeedback>
    ) {
        val examId = _questions.value.firstOrNull()?.examId ?: 0
        val percentage = (correctCount.toFloat() / totalQuestions) * 100

        val result = ExamResult(
            examId = examId,
            correctAnswers = correctCount,
            totalQuestions = totalQuestions,
            score = percentage,
            feedback = feedback
        )

        _examResult.value = result
    }


    fun loadQuestions(examId: Int) {
        _loadingState.value = LoadingState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getQuestionsByExam(examId)
                if (response.isSuccessful) {
                    _questions.value = response.body() ?: emptyList()
                    _loadingState.value = LoadingState.Idle
                } else {
                    _loadingState.value = LoadingState.Error("Error al cargar preguntas")
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun submitExam(examId: Int, answers: List<AnswerSubmission>) {
        Log.d("QuizViewModel", "🔥 submitExam INICIADO (examId=$examId)") // ← Log de prueba
        Log.d("QuizViewModel", "submitExam llamada con examId=$examId y answers=${answers.size}")
        if (answers.isEmpty()) {
            _loadingState.value = LoadingState.Error("No hay respuestas para enviar.")
            return
        }

        // Evitar reenvíos si ya se evaluó este examen
        if (_examResult.value != null && _examResult.value?.examId == examId) {
            _loadingState.value = LoadingState.Error("Este examen ya fue evaluado.")
            return
        }

        _loadingState.value = LoadingState.Loading

        viewModelScope.launch {
            try {
                val submission = ExamSubmission(examId = examId, answers = answers)
                val response = RetrofitClient.apiService.evaluateExam(submission)

                Log.d("QuizViewModel", "Código de respuesta: ${response.code()}") // <- HTTP 200, 400, etc.
                Log.d("QuizViewModel", "¿Es exitosa? ${response.isSuccessful}")  // <- true/false

                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("QuizViewModel", "Resultado asignado: $result") // ← Nuevo log// <- ¿Contiene datos?
                    if (result != null) {
                        _examResult.value = result
                        _loadingState.value = LoadingState.Idle

                        // Limpia las respuestas enviadas (opcional, según tu flujo)
                        _userAnswers.value = emptyList()
                        Log.d("QuizViewModel", "Respuesta recibida: $result")
                    } else {
                        _loadingState.value = LoadingState.Error("Respuesta del servidor vacía.")

                    }
                } else {
                    // Extrae el error detallado del servidor si existe
                    val errorMsg = response.errorBody()?.string() ?: "Error al evaluar el examen."
                    _loadingState.value = LoadingState.Error(errorMsg)
                    Log.e("QuizViewModel", "Error del servidor: $errorMsg") // <- Mensaje de error detallado
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error de red: ${e.message}", e)
                _loadingState.value = LoadingState.Error("Error de conexión: ${e.localizedMessage}")
            }

        }
    }
}