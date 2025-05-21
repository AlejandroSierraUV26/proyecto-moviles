package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Int,
    val title: String,
    val description: String
)
@Serializable
data class CourseRequest(
    val title: String,
    val description: String
)

