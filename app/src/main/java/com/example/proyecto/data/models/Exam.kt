package com.example.proyecto.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Exam(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String?,
    @SerialName("section_id")
    val sectionId: Int,
    @SerialName("difficulty_level")
    val difficultyLevel: Int
) 