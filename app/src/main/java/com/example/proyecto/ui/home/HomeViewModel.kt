package com.example.proyecto.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.Course
import com.example.proyecto.data.models.Section
import com.example.proyecto.data.models.Exam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _userCourses = MutableStateFlow<List<Course>>(emptyList())
    val userCourses: StateFlow<List<Course>> = _userCourses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    private val _courseSections = MutableStateFlow<List<Section>>(emptyList())
    val courseSections: StateFlow<List<Section>> = _courseSections.asStateFlow()

    private val _sectionExams = MutableStateFlow<Map<Int, List<Exam>>>(emptyMap())
    val sectionExams: StateFlow<Map<Int, List<Exam>>> = _sectionExams.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserCourses()
    }

    fun clearState() {
        _userCourses.value = emptyList()
        _selectedCourse.value = null
        _courseSections.value = emptyList()
        _sectionExams.value = emptyMap()
        _error.value = null
        _isLoading.value = false
    }

    fun loadUserCourses() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Cargando cursos del usuario...")
                val courses = RetrofitClient.apiService.getUserCourses()
                Log.d("HomeViewModel", "Cursos cargados: ${courses.size}")
                _userCourses.value = courses
                if (courses.isNotEmpty() && _selectedCourse.value == null) {
                    selectCourse(courses.first())
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar cursos: ${e.message}", e)
                _error.value = "Error al cargar cursos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCourse(course: Course) {
        Log.d("HomeViewModel", "Seleccionando curso: ${course.title} (ID: ${course.id})")
        _selectedCourse.value = course
        loadCourseSections(course.id)
    }

    private fun loadCourseSections(courseId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Cargando secciones para el curso ID: $courseId")
                val response = RetrofitClient.apiService.getSectionsByCourse(courseId)
                if (response.isSuccessful) {
                    val sections = response.body() ?: emptyList()
                    Log.d("HomeViewModel", "Secciones cargadas: ${sections.size}")
                    _courseSections.value = sections
                } else {
                    Log.e("HomeViewModel", "Error al cargar secciones: ${response.code()} - ${response.message()}")
                    _error.value = "Error al cargar secciones: ${response.message()}"
                    _courseSections.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar secciones: ${e.message}", e)
                _error.value = "Error al cargar secciones: ${e.message}"
                _courseSections.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadExamsForSection(sectionId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("HomeViewModel", "Solicitando exámenes para la sección ID: $sectionId")
                val response = RetrofitClient.apiService.getExamsBySection(sectionId)
                Log.d("HomeViewModel", "Respuesta de exámenes: ${response.code()} - ${response.message()}")
                if (response.isSuccessful) {
                    val exams = response.body() ?: emptyList()
                    Log.d("HomeViewModel", "Exámenes recibidos: ${exams.size}")
                    _sectionExams.value = _sectionExams.value.toMutableMap().apply {
                        put(sectionId, exams)
                    }
                } else {
                    Log.e("HomeViewModel", "Error al cargar exámenes: ${response.code()} - ${response.message()}")
                    _error.value = "Error al cargar exámenes: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar exámenes: ${e.message}", e)
                _error.value = "Error al cargar exámenes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 