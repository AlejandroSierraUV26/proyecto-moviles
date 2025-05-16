package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(
    val identifier: String, // Puede ser email o username
    val password: String
)
