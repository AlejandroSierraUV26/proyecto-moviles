package com.example.proyecto.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object SharedEvents {
    private val _courseDeleted = MutableSharedFlow<Int>()
    val courseDeleted: SharedFlow<Int> = _courseDeleted

    suspend fun emitCourseDeleted(courseId: Int) {
        _courseDeleted.emit(courseId)
    }
} 