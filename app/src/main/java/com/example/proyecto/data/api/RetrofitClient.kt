package com.example.proyecto.data.api

import android.content.Context
import android.util.Log
import com.example.proyecto.utils.SecurePreferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Request

object RetrofitClient {
    // Para el emulador de Android, usamos 10.0.2.2 que apunta a localhost de la máquina host
    // Para dispositivo físico, necesitamos usar la IP real de la computadora
    //private const val BASE_URL = "http://192.168.1.60:8080/"
    //private const val BASE_URL = "http://192.168.0.26:8080/"
    const val BASE_URL = "http://192.168.10.18:8080/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val contentType = "application/json".toMediaTypeOrNull() ?: throw IllegalStateException("No se pudo crear el MediaType")

    private lateinit var securePreferences: SecurePreferences
    private var retrofit: Retrofit? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun initialize(context: Context) {
        securePreferences = SecurePreferences(context)
        createRetrofitInstance()
    }


    private fun createRetrofitInstance() {
        val authInterceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val path = originalRequest.url.encodedPath
                Log.d("API_REQUEST", "📡 Enviando petición a $path")

                // Si es auth o recuperación de contraseña, NO agregar token
                if (isAuthRequest(originalRequest) || isPasswordRecoveryRequest(originalRequest)) {
                    return chain.proceed(originalRequest)
                }

                // Para otras rutas, agregar token si existe
                val requestBuilder = originalRequest.newBuilder()
                securePreferences.getToken()?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                return chain.proceed(requestBuilder.build())
            }

            private fun isAuthRequest(request: Request): Boolean {
                val path = request.url.encodedPath
                return path.contains("/auth") ||
                        path.contains("/login") ||
                        path.contains("/register")
            }

            private fun isPasswordRecoveryRequest(request: Request): Boolean {
                val path = request.url.encodedPath
                return path.contains("/password/forgot") ||
                        path.contains("/password/reset")
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor) // Logging se mantiene
            .connectTimeout(60, TimeUnit.SECONDS) // Timeouts se mantienen
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Conversores se mantienen
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val apiService: ApiService
        get() {
            if (retrofit == null) {
                throw IllegalStateException("RetrofitClient no ha sido inicializado. Llama a initialize() primero.")
            }
            return retrofit!!.create(ApiService::class.java)
        }
}