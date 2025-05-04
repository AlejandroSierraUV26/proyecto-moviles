package com.example.proyecto.ui.courses

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf


class CoursesViewModel : ViewModel() {

    private val _courses = mutableStateListOf<String>()
    val courses: List<String> get() = _courses

    private val _selectedCourse = mutableStateOf<String?>(null)
    val selectedCourse: String? get() = _selectedCourse.value

    fun addCourse(name: String) {
        if (!_courses.contains(name)) {
            _courses.add(name)
        }
    }

    fun selectCourse(name: String) {
        _selectedCourse.value = name
    }
}
