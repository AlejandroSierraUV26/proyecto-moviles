// En modules/SetPreguntasViewModel.kt
package com.example.proyecto.ui.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.ApiService
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.QuestionRequest
import com.example.proyecto.data.models.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetPreguntasViewModel : ViewModel() {
    private val _creationState = MutableStateFlow<CreationState>(CreationState.Idle)
    val creationState: StateFlow<CreationState> = _creationState.asStateFlow()

    sealed class CreationState {
        object Idle : CreationState()
        object Loading : CreationState()
        data class Success(val message: String) : CreationState()
        data class Error(val message: String) : CreationState()
    }

    fun createQuestion(
        sectionId: Int,
        difficultyLevel: Int,
        questionText: String,
        options: List<String>,
        correctAnswer: String,
        feedback: String
    ) {
        _creationState.value = CreationState.Loading

        viewModelScope.launch {
            try {
                val request = QuestionRequest(
                    sectionId = sectionId,
                    difficultyLevel = difficultyLevel,
                    questionText = questionText,
                    options = options,
                    correctAnswer = correctAnswer,
                    feedback = feedback
                )

                val response = RetrofitClient.apiService.createQuestion(request)
                if (response.isSuccessful) {
                    _creationState.value = CreationState.Success("Pregunta creada exitosamente")
                } else {
                    _creationState.value = CreationState.Error("Error al crear la pregunta")
                }
            } catch (e: Exception) {
                _creationState.value = CreationState.Error("Error de conexión: ${e.message}")
            }
        }
    }
    fun resetCreationState() {
        _creationState.value = CreationState.Idle
    }
}