package com.example.proyecto.ui.auth


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.ApiResponse
import kotlinx.coroutines.launch

class PasswordRecoveryViewModel : ViewModel() {
    var email by mutableStateOf("")
    var verificationCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var currentStep by mutableStateOf(1) // 1: email, 2: code, 3: new password
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun sendRecoveryEmail() {
        if (email.isBlank()) {
            errorMessage = "Por favor ingresa tu correo electrónico"
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.sendPasswordResetEmail(
                    mapOf("email" to email)
                )

                if (response.success) {
                    currentStep = 2
                    successMessage = "Código enviado a tu correo"
                } else {
                    errorMessage = response.message ?: "Error al enviar el correo"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun verifyCode() {
        if (verificationCode.isBlank()) {
            errorMessage = "Por favor ingresa el código de verificación"
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                // Solo verificación local del código (el backend lo verificará realmente al resetear)
                if (verificationCode.length == 4) {
                    currentStep = 3
                    successMessage = "Código verificado correctamente"
                } else {
                    errorMessage = "El código debe tener 4 dígitos"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPassword() {
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Por favor completa ambos campos"
            return
        }

        if (newPassword != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        if (newPassword.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.resetPassword(
                    mapOf(
                        "email" to email,
                        "token" to verificationCode,
                        "newPassword" to newPassword
                    )
                )

                if (response.success) {
                    successMessage = "Contraseña restablecida correctamente"
                } else {
                    errorMessage = response.message ?: "Error al restablecer la contraseña"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}