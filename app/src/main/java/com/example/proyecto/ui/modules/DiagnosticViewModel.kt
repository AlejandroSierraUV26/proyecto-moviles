package com.example.proyecto.ui.modules


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.DiagnosticQuestion
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.data.models.DiagnosticSubmission
import com.example.proyecto.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class DiagnosticViewModel(application: Application) : AndroidViewModel(application) {
    private val _questions = MutableStateFlow<List<DiagnosticQuestion>>(emptyList())
    val questions: StateFlow<List<DiagnosticQuestion>> = _questions

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
        data class Success(val result: DiagnosticResult) : ResultState()
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

    fun submitDiagnostic(
        courseId: Int,
        level: Int,
        answers: Map<Int, String>,
        authToken: String
    ) {
        viewModelScope.launch {
            _resultState.value = ResultState.Loading
            Log.d("DiagnosticVM", "Enviando diagnóstico - CourseID: $courseId, Level: $level")
            Log.d("DiagnosticVM", "Respuestas a enviar: $answers")

            try {
                val fullToken = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                Log.d("DiagnosticVM", "Token para envío: ${fullToken.take(10)}...")

                val response = RetrofitClient.apiService.submitDiagnosticResults(
                    token = fullToken,
                    submission = DiagnosticSubmission(
                        courseId = courseId,
                        maxLevel = level,
                        answers = answers
                    )
                )

                Log.d("DiagnosticVM", "Respuesta del diagnóstico - Código: ${response.code()}, Éxito: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    response.body()?.let { diagnosticResult ->
                        Log.d("DiagnosticVM", "Resultado recibido: $diagnosticResult")
                        _resultState.value = ResultState.Success(diagnosticResult)
                    } ?: run {
                        Log.e("DiagnosticVM", "Respuesta exitosa pero cuerpo vacío")
                        _resultState.value = ResultState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Token inválido o expirado"
                        400 -> "Solicitud mal formada"
                        else -> "Error del servidor: ${response.code()}"
                    }
                    Log.e("DiagnosticVM", "$errorMsg - Mensaje: ${response.message()}")
                    _resultState.value = ResultState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("DiagnosticVM", "Excepción al enviar diagnóstico: ${e.javaClass.simpleName}", e)
                _resultState.value = ResultState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}