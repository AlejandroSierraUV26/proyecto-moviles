package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterRequest(
    val email: String,
    val username: String,
    val password: String
) 