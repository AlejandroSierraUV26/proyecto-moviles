package com.example.proyecto.ui.modules

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// DiagnosticViewModelFactory.kt
class DiagnosticViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiagnosticViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiagnosticViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
