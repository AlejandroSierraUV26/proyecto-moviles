package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GenerateCourse(
    val title: String,      // Antes: "title"
    val description: String,      // (se mantiene igual o ajusta si es necesario)
    val createdBy: Int? = null,  // (se mantiene igual o ajusta si es necesario)
    val sections: List<GenerateSection>  // Antes: "secciones" (depende del JSON real)
)

@Serializable
data class GenerateSection(
    val title: String,     // Antes: "title"
    val difficultyLevel: Int,     // (se mantiene o ajusta)
    val questions: List<GenerateQuestion>  // Antes: "preguntas"
)

@Serializable
data class GenerateQuestion(
    val questionText: String,     // Antes: "textoPregunta"
    val options: List<String>,    // (se mantiene o ajusta)
    val correctAnswer: String,    // Antes: "respuestaCorrecta"
    val feedback: String          // Antes: "retroalimentacion"
)