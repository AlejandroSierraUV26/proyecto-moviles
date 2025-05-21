package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val id: Int,
    val courseId: Int, // en vez de moduleId
    val title: String,
    val difficultyLevel: Int
)

@Serializable
data class SectionRequest(
    val courseId: Int,
    val title: String,
    val difficultyLevel: Int
)

