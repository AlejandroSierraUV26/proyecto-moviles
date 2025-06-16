package com.example.proyecto.ui.modules

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class DiagnosticResultsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiagnosticResultsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiagnosticResultsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}