package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val description: String
)
