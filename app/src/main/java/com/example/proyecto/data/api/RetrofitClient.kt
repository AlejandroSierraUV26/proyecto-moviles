package com.example.proyecto.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Para el emulador de Android, usamos 10.0.2.2 que apunta a localhost de la máquina host
    // Para dispositivo físico, necesitamos usar la IP real de la computadora
    private const val BASE_URL = "http://192.168.1.60:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
} 