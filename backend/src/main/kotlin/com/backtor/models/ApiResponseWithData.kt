// ApiResponseWithData.kt (nuevo archivo)
package com.backtor.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseWithData<T>(
    val success: Boolean,
    val message: String,
    val data: T
)