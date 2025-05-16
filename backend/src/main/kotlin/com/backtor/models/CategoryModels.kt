package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int,
    val title: String
)
@Serializable
data class CategoryRequest(
    val title: String
)