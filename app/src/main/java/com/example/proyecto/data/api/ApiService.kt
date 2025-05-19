package com.example.proyecto.data.api

import com.example.proyecto.data.models.UserRegisterRequest
import com.example.proyecto.data.models.UserLoginRequest
import com.example.proyecto.data.models.LoginResponse
import com.example.proyecto.data.models.RegisterResponse
import com.example.proyecto.data.models.UserProfile
import com.example.proyecto.data.models.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @POST("api/register")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<RegisterResponse>

    @POST("api/login")
    suspend fun loginUser(@Body request: UserLoginRequest): Response<LoginResponse>

    @GET("api/profile")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("api/profile/update")
    suspend fun updateProfile(
        @Body request: Map<String, String>
    ): Response<ApiResponse>

    @DELETE("api/delete")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Query("password") password: String
    ): ApiResponse
} 