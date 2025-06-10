package com.example.proyecto.data.models

data class GoogleAuthResponse(
    val token: String,
    val username: String,
    val email: String
)

data class ApiResponseWithData<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
