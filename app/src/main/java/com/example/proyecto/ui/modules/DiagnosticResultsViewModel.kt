package com.example.proyecto.ui.modules

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.DiagnosticFeedback
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.data.models.DiagnosticSubmission
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder

class DiagnosticResultsViewModel(application: Application) : AndroidViewModel(application) {
    private val _feedback = MutableStateFlow<DiagnosticFeedback?>(null)
    val feedback: StateFlow<DiagnosticFeedback?> = _feedback.asStateFlow()
    private val _state = MutableStateFlow<ResultState>(ResultState.Idle)
    val state: StateFlow<ResultState> = _state.asStateFlow()
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
    private val _averageScore = MutableStateFlow(0f)
    val averageScore: StateFlow<Float> = _averageScore.asStateFlow()
    private val _results = MutableStateFlow<List<DiagnosticResult>>(emptyList())
    val results: StateFlow<List<DiagnosticResult>> = _results.asStateFlow()
    private val _overallResult = MutableStateFlow("")
    val overallResult: StateFlow<String> = _overallResult.asStateFlow()
    private val _recommendedSection = MutableStateFlow("")
    val recommendedSection: StateFlow<String> = _recommendedSection.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setArgument(
        levelTested: Int,
        passed: Boolean,
        score: Double,
        startingSection: String,
        message: String,
        feedbackJson: String
    ) {
        _levelTested.value = levelTested
        _passed.value = passed
        _score.value = score
        _startingSection.value = startingSection
        _message.value = message

        try {
            // Limpiar el JSON antes de parsear
            val cleanJson = feedbackJson
                .replace("+", " ")
                .replace("%22", "\"")

            val feedback = Gson().fromJson(cleanJson, DiagnosticFeedback::class.java)
            _feedback.value = feedback
            processFeedbackJson(cleanJson) // Procesar también aquí para mantener consistencia
        } catch (e: Exception) {
            Log.e("DiagnosticVM", "Error parsing feedback", e)
            // Opcional: Proporcionar valores por defecto
            _results.value = emptyList()
            _overallResult.value = "No se pudieron cargar los resultados"
            _recommendedSection.value = ""
        }
    }

    fun processFeedbackJson(feedbackJson: String?) {
        if (feedbackJson.isNullOrEmpty()) {
            Log.e("DiagnosticResultsVM", "JSON vacío o nulo")
            return
        }

        try {
            // Primero limpiamos posibles problemas de formato
            val cleanJson = feedbackJson
                .replace("+", " ") // Reemplaza + por espacios
                .replace("%22", "\"") // Asegura comillas correctas
                .replace("%20", " ") // Reemplaza espacios codificados

            // Parseamos directamente con Gson
            val feedback = Gson().fromJson(cleanJson, DiagnosticFeedback::class.java)

            // Actualizar estados
            _results.value = feedback.results
            _overallResult.value = feedback.overallResult
            _recommendedSection.value = feedback.recommendedStartingSection

            // Calcular promedio
            val avg = feedback.results
                .takeIf { it.isNotEmpty() }
                ?.map { it.score }
                ?.average()
                ?.toFloat() ?: 0f

            _averageScore.value = avg
            _passed.value = feedback.results.all { it.passed }

            Log.d("DiagnosticResultsVM", "Feedback procesado: ${feedback.results.size} resultados")
        } catch (e: Exception) {
            Log.e("DiagnosticResultsVM", "Error procesando JSON: ${e.message}", e)
            Log.e("DiagnosticResultsVM", "JSON recibido: $feedbackJson")
        }
    }

    fun setFeedback(feedback: DiagnosticFeedback) {
        _isLoading.value = true

        try {
            // Datos directos del backend
            _results.value = feedback.results
            _overallResult.value = feedback.overallResult
            _recommendedSection.value = feedback.recommendedStartingSection

            // Calcular promedio general (ya viene calculado del backend en cada nivel)
            val avg = feedback.results
                .takeIf { it.isNotEmpty() }
                ?.map { it.score }
                ?.average()
                ?.toFloat()
                ?: 0f

            _averageScore.value = avg
            _passed.value = feedback.results.all { it.passed } // El backend ya determina si pasó cada nivel

            Log.d("DiagnosticVM", "Feedback procesado. Promedio: $avg%, Aprobado: ${_passed.value}")
        } catch (e: Exception) {
            Log.e("DiagnosticVM", "Error procesando feedback", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun loadResults(feedback: DiagnosticFeedback) {
        _isLoading.value = true
        try {
            _results.value = feedback.results
            _overallResult.value = feedback.overallResult ?: ""
            _recommendedSection.value = feedback.recommendedStartingSection ?: ""
        } finally {
            _isLoading.value = false
        }
    }

    sealed class ResultState {
        object Idle : ResultState()
        object Loading : ResultState()
        data class Success(val feedback: DiagnosticFeedback) : ResultState()
        data class Error(val message: String) : ResultState()
    }

    fun submitDiagnostic(courseId: Int, level: Int, answers: Map<Int, String>, authToken: String) {
        _state.value = ResultState.Loading
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val cleanToken = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                val response = RetrofitClient.apiService.submitDiagnosticResults(
                    token = cleanToken,
                    submission = DiagnosticSubmission(
                        courseId = courseId,
                        maxLevel = level,
                        answers = answers
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { feedback ->
                        Log.d("DiagnosticVM", "Feedback recibido con ${feedback.results.size} resultados")
                        _feedback.value = feedback
                        _state.value = ResultState.Success(feedback)
                        loadResults(feedback)

                        // Actualizar todos los estados relevantes
                        _passed.value = feedback.results.all { it.passed }
                        _averageScore.value = feedback.results.map { it.score }.average().toFloat()
                    } ?: run {
                        _state.value = ResultState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    // Manejo de errores mejorado
                    val errorMsg = try {
                        response.errorBody()?.string() ?: "Error sin mensaje"
                    } catch (e: Exception) {
                        "Error al leer mensaje de error"
                    }
                    _state.value = ResultState.Error("Error ${response.code()}: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = "Error de conexión: ${e.localizedMessage ?: "Error desconocido"}"
                Log.e("DiagnosticVM", errorMsg, e)
                _state.value = ResultState.Error(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }
}