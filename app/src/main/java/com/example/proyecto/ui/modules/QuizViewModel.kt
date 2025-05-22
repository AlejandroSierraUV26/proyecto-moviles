package com.example.proyecto.ui.modules

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class QuizViewModel : ViewModel() {
    private val _aciertos = MutableStateFlow(0)
    val aciertos: StateFlow<Int> = _aciertos

    private val _progreso = MutableStateFlow(0)
    val progreso: StateFlow<Int> = _progreso

    private val _recomendaciones = MutableStateFlow("")
    val recomendaciones: StateFlow<String> = _recomendaciones

    fun setResultados(aciertos: Int, progreso: Int, recomendaciones: String) {
        _aciertos.value = aciertos
        _progreso.value = progreso
        _recomendaciones.value = recomendaciones
    }
}