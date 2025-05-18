package com.example.proyecto.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.UserLoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    fun loginUser(emailOrUsername: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.apiService.loginUser(
                    UserLoginRequest(identifier = emailOrUsername, password = password)
                )
                if (response.isSuccessful) {
                    _loginState.value = LoginState.Success(response.body()?.message ?: "Login exitoso")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _loginState.value = LoginState.Error(
                        response.body()?.message ?: errorBody ?: "Error en el login"
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
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
} 