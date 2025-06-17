package com.example.proyecto.ui.modules


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.DiagnosticFeedback
import com.example.proyecto.data.models.DiagnosticQuestion
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.data.models.DiagnosticSubmission
import com.example.proyecto.data.models.Question
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class DiagnosticViewModel(application: Application) : AndroidViewModel(application) {
    private val _levelTested = MutableStateFlow(0)
    val levelTested: StateFlow<Int> = _levelTested.asStateFlow()
    private val _passed = MutableStateFlow(false)
    val passed: StateFlow<Boolean> = _passed.asStateFlow()
    private val _score = MutableStateFlow(0.0)
    val score: StateFlow<Double> = _score.asStateFlow()
    private val _startingSection = MutableStateFlow("")
    val startingSection: StateFlow<String> = _startingSection.asStateFlow()
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()


    private val _feedback = MutableStateFlow<DiagnosticFeedback?>(null)
    val feedback: StateFlow<DiagnosticFeedback?> = _feedback.asStateFlow()
    private val _questions = MutableStateFlow<List<DiagnosticQuestion>>(emptyList())
    val questions: StateFlow<List<DiagnosticQuestion>> = _questions
    private val _diagnosticFeedback = MutableStateFlow<DiagnosticFeedback?>(null)
    val diagnosticFeedback: StateFlow<DiagnosticFeedback?> = _diagnosticFeedback.asStateFlow()
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState
    private val _resultState = MutableStateFlow<ResultState>(ResultState.Idle)
    val resultState: StateFlow<ResultState> = _resultState


    // Variable para almacenar el token
    private var authToken: String? = null

    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        data class Success(val questions: List<DiagnosticQuestion>) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    fun setAuthToken(token: String) {
        this.authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        Log.d("DiagnosticVM", "Token establecido: ${authToken?.take(10)}... (longitud: ${authToken?.length})")
    }



    sealed class ResultState {
        object Idle : ResultState()
        object Loading : ResultState()
        // Cambiar de DiagnosticResult a DiagnosticFeedback
        data class Success(val feedback: DiagnosticFeedback) : ResultState()
        data class Error(val message: String) : ResultState()
    }


    fun loadQuestions(courseId: Int, level: Int) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            Log.d("DiagnosticVM", "Iniciando carga de preguntas - CourseID: $courseId, Level: $level")

            try {
                if (authToken == null) {
                    Log.e("DiagnosticVM", "Error: Token de autenticación es nulo")
                    _loadingState.value = LoadingState.Error("No autenticado")
                    return@launch
                }

                Log.d("DiagnosticVM", "Realizando solicitud con token: ${authToken?.take(10)}...")
                val response = RetrofitClient.apiService.getDiagnosticQuestions(
                    token = authToken!!,
                    courseId = courseId,
                    level = level
                )

                Log.d("DiagnosticVM", "Respuesta recibida - Código: ${response.code()}, Éxito: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    response.body()?.let { questions ->
                        if (questions.isEmpty()) {
                            Log.w("DiagnosticVM", "Respuesta exitosa pero sin preguntas")
                            _loadingState.value = LoadingState.Error("No hay preguntas para este nivel")
                        } else {
                            Log.d("DiagnosticVM", "Preguntas recibidas: ${questions.size}")
                            _questions.value = questions
                            _loadingState.value = LoadingState.Success(questions)
                        }
                    } ?: run {
                        Log.e("DiagnosticVM", "Respuesta exitosa pero cuerpo vacío")
                        _loadingState.value = LoadingState.Error("Respuesta vacía")
                    }
                } else {
                    if (!response.isSuccessful) {
                        val errorMsg = when (response.code()) {
                            404 -> "Endpoint no encontrado: ${response.raw().request.url}"
                            else -> "Error ${response.code()}"
                        }
                        Log.e("DiagnosticVM", "$errorMsg - Mensaje: ${response.message()}")
                        _loadingState.value = LoadingState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e("DiagnosticVM", "Excepción al cargar preguntas: ${e.javaClass.simpleName}", e)
                _loadingState.value = LoadingState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun submitDiagnostic(courseId: Int, level: Int, answers: Map<Int, String>) {
        _resultState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val token = authToken ?: throw Exception("Token no disponible")

                // Primero validar localmente las respuestas
                val questions = _questions.value
                // Validación local
                val validationResults = mutableMapOf<Int, Boolean>().apply {
                    answers.forEach { (questionId, selectedAnswer) ->
                        val question = questions.find { it.id == questionId }
                        // Si no hay respuesta correcta, asumir que la seleccionada es correcta
                        val isCorrect = question?.correctAnswer?.let { it == selectedAnswer } ?: true
                        put(questionId, isCorrect)
                    }
                }

                answers.forEach { (questionId, selectedAnswer) ->
                    val question = questions.find { it.id == questionId }
                    question?.let {
                        validationResults[questionId] = (selectedAnswer == it.correctAnswer)
                    }
                }

                Log.d("DiagnosticVM", "Validación local: $validationResults")

                // Luego enviar al servidor
                val response = RetrofitClient.apiService.submitDiagnosticResults(
                    token = token,
                    submission = DiagnosticSubmission(
                        courseId = courseId,
                        maxLevel = level,
                        answers = answers
                    )
                )
                if (response.isSuccessful) {
                    response.body()?.let { feedback ->
                        _resultState.value = ResultState.Success(feedback)
                    } ?: run {
                        _resultState.value = ResultState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    _resultState.value = ResultState.Error("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                _resultState.value = ResultState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }
}