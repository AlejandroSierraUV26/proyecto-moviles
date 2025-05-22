package com.example.proyecto.data.api

import android.content.Context
import com.example.proyecto.utils.SecurePreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {
    // Para el emulador de Android, usamos 10.0.2.2 que apunta a localhost de la máquina host
    // Para dispositivo físico, necesitamos usar la IP real de la computadora
    private const val BASE_URL = "http://192.168.1.60:8080/"

    private lateinit var securePreferences: SecurePreferences

    fun initialize(context: Context) {
        securePreferences = SecurePreferences(context)
    }

    private val authInterceptor = object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = securePreferences.getToken()
            val request = chain.request().newBuilder()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }
            return chain.proceed(request.build())
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
} 