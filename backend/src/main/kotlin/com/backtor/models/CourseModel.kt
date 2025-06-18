package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Int,
    val title: String,
    val description: String,
    val createdBy: Int? = null
)
@Serializable
data class CourseRequest(
    val id: Int,
    val title: String,
    val description: String,
    val createdBy: Int? = null
)

