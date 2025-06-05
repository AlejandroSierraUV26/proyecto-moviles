package com.example.proyecto.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val id: Int,
    @SerialName("courseId")
    val courseId: Int,
    val title: String,
    @SerialName("difficultyLevel")
    val difficultyLevel: Int
) 