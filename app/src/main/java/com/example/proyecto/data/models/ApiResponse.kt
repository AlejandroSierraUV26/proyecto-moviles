package com.example.proyecto.data.models

data class ApiResponse(
    val message: String,
    val token: String? = null
)

data class LoginResponse(
    val token: String
)

data class RegisterResponse(
    val message: String,
    val token: String
) 