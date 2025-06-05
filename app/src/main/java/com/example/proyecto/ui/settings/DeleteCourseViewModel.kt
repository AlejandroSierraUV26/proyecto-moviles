package com.example.proyecto.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.Course
import com.example.proyecto.utils.SharedEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeleteCourseViewModel : ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserCourses()
    }

    fun loadUserCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userCourses = RetrofitClient.apiService.getUserCourses()
                _courses.value = userCourses
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar los cursos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                RetrofitClient.apiService.removeCourseFromUser(mapOf("courseId" to courseId))
                // Emitir el evento de eliminación
                SharedEvents.emitCourseDeleted(courseId)
                // Actualizar la lista de cursos después de eliminar
                loadUserCourses()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al eliminar el curso: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 