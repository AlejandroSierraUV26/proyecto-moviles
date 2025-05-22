package com.example.proyecto.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class LoginResponse(
    val token: String
)

data class RegisterResponse(
    val message: String,
    val token: String
) 