package com.example.proyecto.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.UserRegisterRequest
import com.example.proyecto.data.models.RegisterResponse
import com.example.proyecto.utils.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState
    private val securePreferences = SecurePreferences(application)

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
                        securePreferences.saveToken(registerResponse.token)
                        securePreferences.saveUserEmail(email)
                        _registerState.value = RegisterState.Success(registerResponse.message)
                    } else {
                        _registerState.value = RegisterState.Error("Error: Respuesta inválida del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _registerState.value = RegisterState.Error(
                        errorBody ?: "Error en el registro"
                    )
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _registerState.value = RegisterState.Error(
                    errorBody ?: "Error en el registro"
                )
            } catch (e: IOException) {
                _registerState.value = RegisterState.Error("Error de conexión: ${e.message}")
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Error inesperado: ${e.message}")
            }
        }
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
} 