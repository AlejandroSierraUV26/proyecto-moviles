package com.example.proyecto.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(
    val identifier: String,
    val password: String
) 