package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val email: String,
    val password: String
)
