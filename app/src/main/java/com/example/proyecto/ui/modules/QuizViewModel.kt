package com.example.proyecto.ui.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.AnswerSubmission
import com.example.proyecto.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class QuizViewModel : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

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

    // Añade esta función a tu QuizViewModel
    fun loadDiagnosticQuestions(courseId: Int, level: String) {
        _loadingState.value = LoadingState.Loading
        viewModelScope.launch {
            try {
                // 1. Obtener todas las preguntas del curso (usando endpoint existente)
                val allQuestions = mutableListOf<Question>()

                // Primero obtener las secciones del curso
                val sectionsResponse = RetrofitClient.apiService.getSectionsByCourse(courseId)
                if (!sectionsResponse.isSuccessful) {
                    _loadingState.value = LoadingState.Error("Error al obtener secciones del curso")
                    return@launch
                }

                val sections = sectionsResponse.body() ?: emptyList()

                // 2. Filtrar secciones según nivel (ejemplo básico)
                val targetSections = when(level.lowercase()) {
                    "intermediate" -> sections.take(2)
                    "advanced" -> sections
                    else -> sections.take(1) // básico por defecto
                }

                // 3. Obtener preguntas de cada sección
                for (section in targetSections) {
                    val examsResponse = RetrofitClient.apiService.getExamsBySection(section.id)
                    if (examsResponse.isSuccessful) {
                        examsResponse.body()?.forEach { exam ->
                            val questionsResponse = RetrofitClient.apiService.getQuestionsByExam(exam.id)
                            if (questionsResponse.isSuccessful) {
                                questionsResponse.body()?.let { allQuestions.addAll(it) }
                            }
                        }
                    }
                }

                // 4. Seleccionar preguntas según nivel
                val diagnosticQuestions = when(level.lowercase()) {
                    "intermediate" -> allQuestions.take(10) // 10 preguntas para intermedio
                    "advanced" -> allQuestions.shuffled().take(15) // 15 preguntas aleatorias para avanzado
                    else -> allQuestions.take(5) // 5 preguntas para básico
                }

                if (diagnosticQuestions.isEmpty()) {
                    _loadingState.value = LoadingState.Error("No se encontraron preguntas para este nivel")
                } else {
                    _questions.value = diagnosticQuestions
                    _loadingState.value = LoadingState.Idle
                }

            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Error: ${e.message ?: "Error desconocido"}")
            }
        }
    }
    fun clearQuizState() {
        _questions.value = emptyList()
        _userAnswers.value = emptyList()
        _loadingState.value = LoadingState.Idle
    }
    fun setAllAnswers(answers: List<AnswerSubmission>) {
        _userAnswers.value = answers
    }
}