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

    fun submitDiagnostic(courseId: Int, level: String, answers: Map<Int, String>, authToken: String) {
        _state.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.submitDiagnostic(
                    "Bearer $authToken",
                    DiagnosticSubmission(
                        courseId = courseId,
                        level = level,
                        answers = answers
                    )
                )

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        // Validación adicional de los datos recibidos
                        if (result.startingSection.isNotEmpty() && result.message.isNotEmpty()) {
                            _result.value = result
                            _state.value = ResultState.Success(result)
                        } else {
                            _state.value = ResultState.Error("Datos incompletos del servidor")
                        }
                    } ?: run {
                        _state.value = ResultState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    // Manejo de errores mejorado
                    _state.value = when (response.code()) {
                        401 -> ResultState.Error("Sesión expirada, por favor inicia sesión nuevamente")
                        404 -> ResultState.Error("Recurso no encontrado")
                        in 500..599 -> ResultState.Error("Error del servidor, intenta más tarde")
                        else -> ResultState.Error("Error desconocido: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _state.value = ResultState.Error("Error de conexión: ${e.localizedMessage ?: "Error desconocido"}")
                Log.e("DiagnosticError", "Error al enviar diagnóstico", e)
            }
        }
    }

}