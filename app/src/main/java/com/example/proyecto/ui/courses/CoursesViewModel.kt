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
        loadAvailableCourses()
        loadUserCourses()
    }

    private fun loadAvailableCourses() {
        viewModelScope.launch {
            try {
                val courses = RetrofitClient.apiService.getAllCourses()
                _availableCourses.value = courses
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    private fun loadUserCourses() {
        viewModelScope.launch {
            try {
                val courses = RetrofitClient.apiService.getUserCourses()
                _userCourses.value = courses
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun addCourseToUser(courseId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.addCourseToUser(mapOf("courseId" to courseId))
                loadUserCourses() // Recargar cursos del usuario
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }
}
