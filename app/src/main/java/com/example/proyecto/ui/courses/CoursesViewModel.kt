package com.example.proyecto.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.ApiService
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoursesViewModel : ViewModel() {
    private val _availableCourses = MutableStateFlow<List<Course>>(emptyList())
    val availableCourses: StateFlow<List<Course>> = _availableCourses.asStateFlow()

    private val _userCourses = MutableStateFlow<List<Course>>(emptyList())
    val userCourses: StateFlow<List<Course>> = _userCourses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    init {
        // No cargamos cursos en el init para evitar cargas innecesarias
    }

    fun loadAvailableCourses() {
        viewModelScope.launch {
            try {
                // Limpiar los cursos actuales antes de cargar los nuevos
                _availableCourses.value = emptyList()
                
                val courses = RetrofitClient.apiService.getAllCourses()
                _availableCourses.value = courses
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun loadUserCourses() {
        viewModelScope.launch {
            try {
                // Limpiar los cursos actuales antes de cargar los nuevos
                _userCourses.value = emptyList()
                _selectedCourse.value = null
                
                val courses = RetrofitClient.apiService.getUserCourses()
                _userCourses.value = courses
                // Si hay cursos y no hay curso seleccionado, seleccionar el primero
                if (courses.isNotEmpty() && _selectedCourse.value == null) {
                    _selectedCourse.value = courses.first()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun addCourseToUser(courseId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.addCourseToUser(mapOf("courseId" to courseId))
                // Recargar los cursos del usuario después de agregar uno nuevo
                loadUserCourses()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun clearCourses() {
        _userCourses.value = emptyList()
        _selectedCourse.value = null
        _availableCourses.value = emptyList()
    }
}
