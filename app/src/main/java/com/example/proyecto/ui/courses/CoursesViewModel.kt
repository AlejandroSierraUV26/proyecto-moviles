package com.example.proyecto.ui.courses

import android.util.Log
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
                _availableCourses.value = RetrofitClient.apiService.getAllCourses()
            } catch (e: Exception) {
                Log.e("COURSE_FLOW", "Error cargando cursos disponibles: ${e.message}")
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
        Log.d("COURSE_FLOW", "Curso seleccionado en CoursesViewModel: ${course.title}")
    }

    fun addCourseToUser(courseId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.addCourseToUser(mapOf("courseId" to courseId))
                Log.d("COURSE_FLOW", "Curso $courseId agregado al usuario")
            } catch (e: Exception) {
                Log.e("COURSE_FLOW", "Error agregando curso al usuario: ${e.message}")
            }
        }
    }

    fun addCourseWithNavigation(
        course: Course,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Seleccionar el curso
                selectCourse(course)
                // 2. Añadir el curso al usuario
                addCourseToUser(course.id)
                // 3. Notificar éxito
                onSuccess()
            } catch (e: Exception) {
                // 4. Notificar error
                onError("Error al seleccionar curso: ${e.message}")
            }
        }
    }

    fun clearCourses() {
        _userCourses.value = emptyList()
        _selectedCourse.value = null
        _availableCourses.value = emptyList()
    }
}
