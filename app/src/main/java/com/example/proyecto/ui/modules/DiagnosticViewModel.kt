package com.example.proyecto.ui.modules


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.data.models.DiagnosticSubmission
import com.example.proyecto.data.models.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class DiagnosticViewModel(application: Application) : AndroidViewModel(application) {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState

    private val _resultState = MutableStateFlow<ResultState>(ResultState.Idle)
    val resultState: StateFlow<ResultState> = _resultState

    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        data class Success(val questions: List<Question>) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    sealed class ResultState {
        object Idle : ResultState()
        object Loading : ResultState()
        data class Success(val result: DiagnosticResult) : ResultState()
        data class Error(val message: String) : ResultState()
    }

    fun loadQuestions(courseId: Int, level: String) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            try {
                val response = RetrofitClient.apiService.getDiagnosticQuestions(courseId, level)

                if (response.isSuccessful) {
                    val questions = response.body() ?: emptyList()
                    if (questions.isEmpty()) {
                        _loadingState.value = LoadingState.Error("No hay preguntas disponibles")
                    } else {
                        _questions.value = questions
                        _loadingState.value = LoadingState.Success(questions)
                    }
                } else {
                    _loadingState.value = LoadingState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun submitDiagnostic(
        courseId: Int,
        level: String,
        answers: Map<Int, String>,
        authToken: String
    ) {
        viewModelScope.launch {
            _resultState.value = ResultState.Loading
            try {
                val response = RetrofitClient.apiService.submitDiagnosticResults(
                    token = "Bearer $authToken",
                    submission = DiagnosticSubmission(
                        courseId = courseId,
                        level = level,
                        answers = answers
                    )
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        _resultState.value = ResultState.Success(it)
                    } ?: run {
                        _resultState.value = ResultState.Error("Respuesta vacía")
                    }
                } else {
                    _resultState.value = ResultState.Error("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                _resultState.value = ResultState.Error("Error de red: ${e.message}")
            }
        }
    }
}