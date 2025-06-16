package com.example.proyecto.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.UserRegisterRequest
import com.example.proyecto.data.models.RegisterResponse
import com.example.proyecto.utils.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState
    private val securePreferences = SecurePreferences(application)
    private val _authToken = MutableStateFlow<String?>(null) // Añade esta línea
    val authToken: StateFlow<String?> = _authToken.asStateFlow() // Añade esta línea


    fun registerUser(email: String, username: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val response = RetrofitClient.apiService.registerUser(
                    UserRegisterRequest(email, username, password)
                )
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null) {
                        // Primero limpiamos la sesión anterior
                        securePreferences.clearSession()
                        // Luego guardamos el nuevo token y email
                        securePreferences.saveToken(registerResponse.token)
                        securePreferences.saveUserEmail(email)
                        updateToken(registerResponse.token)
                        _registerState.value = RegisterState.Success(registerResponse.message)
                    }else {
                        _registerState.value = RegisterState.Error("Error: Respuesta inválida del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _registerState.value = RegisterState.Error(
                        errorBody ?: "Error en el registro"
                    )
                }
            } catch (e: Exception) {
                handleRegisterError(e)
            }
        }
    }
    private fun updateToken(newToken: String) {
        viewModelScope.launch {
            try {
                // Usa la instancia de securePreferences, no la clase
                securePreferences.saveToken(newToken)
                _authToken.value = newToken
                Log.d("AUTH", "Token actualizado: ${newToken.take(10)}...")
            } catch (e: Exception) {
                Log.e("AUTH", "Error actualizando token", e)
            }
        }
    }

    private fun handleRegisterError(e: Exception) {
        _registerState.value = when (e) {
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                RegisterState.Error(errorBody ?: "Error en el registro")
            }
            is IOException -> RegisterState.Error("Error de conexión: ${e.message}")
            else -> RegisterState.Error("Error inesperado: ${e.message}")
        }
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
} 