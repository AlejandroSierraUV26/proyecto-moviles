package com.example.proyecto.ui.modules

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.data.models.DiagnosticSubmission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosticResultsViewModel (application: Application) : AndroidViewModel(application){
    private val _result = MutableStateFlow<DiagnosticResult?>(null)
    val result: StateFlow<DiagnosticResult?> = _result

    private val _state = MutableStateFlow<ResultState>(ResultState.Idle)
    val state: StateFlow<ResultState> = _state

    sealed class ResultState {
        object Idle : ResultState()
        object Loading : ResultState()
        data class Success(val data: DiagnosticResult) : ResultState()
        data class Error(val message: String) : ResultState()
    }

    init {
        _state.value = ResultState.Idle
        _result.value = null
    }

    fun submitDiagnostic(courseId: Int, level: Int, answers: Map<Int, String>, authToken: String) {
        _state.value = ResultState.Loading
        Log.d("DiagnosticResultsVM", "Enviando diagnóstico - CourseID: $courseId, Level: $level")
        Log.d("DiagnosticResultsVM", "Respuestas: $answers")

        viewModelScope.launch {
            try {
                val fullToken = if (authToken.startsWith("Bearer ")) authToken else "Bearer $authToken"
                Log.d("DiagnosticResultsVM", "Token utilizado: ${fullToken.take(10)}...")

                val response = RetrofitClient.apiService.submitDiagnostic(
                    fullToken,
                    DiagnosticSubmission(
                        courseId = courseId,
                        maxLevel = level,
                        answers = answers
                    )
                )

                Log.d("DiagnosticResultsVM", "Respuesta recibida - Código: ${response.code()}, Éxito: ${response.isSuccessful()}")

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        if (result.startingSection.isNotEmpty() && result.message.isNotEmpty()) {
                            Log.d("DiagnosticResultsVM", "Resultado válido recibido: $result")
                            _result.value = result
                            _state.value = ResultState.Success(result)
                        } else {
                            Log.e("DiagnosticResultsVM", "Datos incompletos del servidor")
                            _state.value = ResultState.Error("Datos incompletos del servidor")
                        }
                    } ?: run {
                        Log.e("DiagnosticResultsVM", "Respuesta vacía del servidor")
                        _state.value = ResultState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Sesión expirada (código 401)"
                        404 -> "Recurso no encontrado (código 404)"
                        in 500..599 -> "Error del servidor (código ${response.code()})"
                        else -> "Error desconocido (código ${response.code()})"
                    }
                    Log.e("DiagnosticResultsVM", errorMsg)
                    _state.value = ResultState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("DiagnosticResultsVM", "Excepción al enviar diagnóstico: ${e.javaClass.simpleName}", e)
                _state.value = ResultState.Error("Error de conexión: ${e.localizedMessage ?: "Error desconocido"}")
            }
        }
    }
}