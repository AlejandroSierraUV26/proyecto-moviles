package com.example.proyecto.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.UserLoginRequest
import com.example.proyecto.data.models.LoginResponse
import com.example.proyecto.utils.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.example.proyecto.data.models.GoogleAuthRequest


class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState
    private val securePreferences = SecurePreferences(application)

    fun loginUser(emailOrUsername: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.apiService.loginUser(
                    UserLoginRequest(identifier = emailOrUsername, password = password)
                )
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Limpiar la sesión anterior antes de guardar la nueva
                        securePreferences.clearSession()
                        securePreferences.saveToken(loginResponse.token)
                        securePreferences.saveUserEmail(emailOrUsername)
                        _loginState.value = LoginState.Success("Login exitoso")
                    } else {
                        _loginState.value =
                            LoginState.Error("Error: Respuesta inválida del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _loginState.value = LoginState.Error(
                        errorBody ?: "Error en el login"
                    )
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _loginState.value = LoginState.Error(
                    errorBody ?: "Error en el login"
                )
            } catch (e: IOException) {
                _loginState.value = LoginState.Error("Error de conexión: ${e.message}")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        Log.d("GOOGLE_AUTH", "🌐 Iniciando loginWithGoogle. Token: ${idToken.take(20)}... (${idToken.length} chars total)")

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                if (idToken.isBlank()) {
                    throw IllegalArgumentException("Token vacío")
                }

                val request = GoogleAuthRequest(idToken = idToken)
                Log.d("GOOGLE_AUTH", "📤 Enviando request con token: ${request.idToken.take(20)}...")

                val response = try {
                    RetrofitClient.apiService.authWithGoogle(request)
                } catch (e: Exception) {
                    Log.e("GOOGLE_AUTH", "🔴 Error en la llamada API", e)
                    throw IOException("Error de conexión: ${e.message}")
                }

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                    Log.e("GOOGLE_AUTH", "❌ Error del servidor: $errorBody")
                    throw IOException("Error del servidor: ${response.code()} - $errorBody")
                }

                val loginResponse = response.body()
                when {
                    loginResponse == null -> throw NullPointerException("Respuesta vacía del servidor")
                    !loginResponse.success -> throw Exception(loginResponse.message ?: "Error en autenticación")
                    loginResponse.data.token.isBlank() -> throw IllegalStateException("Token vacío en la respuesta")
                    else -> {
                        securePreferences.apply {
                            clearSession()
                            saveToken(loginResponse.data.token)
                            saveUserEmail(loginResponse.data.email)
                        }
                        _loginState.value = LoginState.Success("Login exitoso con Google")
                        Log.d("GOOGLE_AUTH", "🎉 Autenticación exitosa para ${loginResponse.data.email}")
                    }
                }
            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "🔥 Error durante loginWithGoogle", e)
                _loginState.value = LoginState.Error(
                    when (e) {
                        is IllegalArgumentException -> "Token de Google inválido"
                        is IOException -> "Error de conexión: ${e.message}"
                        else -> "Error: ${e.message ?: "Desconocido"}"
                    }
                )
            }
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
} 