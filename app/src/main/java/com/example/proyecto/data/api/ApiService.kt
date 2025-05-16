package com.example.proyecto.data.api

import com.example.proyecto.data.models.UserRegisterRequest
import com.example.proyecto.data.models.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<ApiResponse>
} 