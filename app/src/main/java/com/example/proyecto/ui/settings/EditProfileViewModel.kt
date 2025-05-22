package com.example.proyecto.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.data.models.UserProfile
import com.example.proyecto.data.models.ApiResponse
import com.example.proyecto.utils.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState
    
    private val securePreferences = SecurePreferences(application)
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val token = securePreferences.getToken()
                if (token == null) {
                    _profileState.value = ProfileState.Error("No hay sesión activa")
                    return@launch
                }
                
                val response = RetrofitClient.apiService.getUserProfile()
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile != null) {
                        _profileState.value = ProfileState.Success(userProfile)
                    } else {
                        _profileState.value = ProfileState.Error("Error: Respuesta inválida del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _profileState.value = ProfileState.Error(
                        errorBody ?: "Error al cargar el perfil"
                    )
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _profileState.value = ProfileState.Error(
                    errorBody ?: "Error al cargar el perfil"
                )
            } catch (e: IOException) {
                _profileState.value = ProfileState.Error("Error de conexión: ${e.message}")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun updateProfile(
        username: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val token = securePreferences.getToken()
                if (token == null) {
                    _profileState.value = ProfileState.Error("No se encontró el token de sesión")
                    return@launch
                }

                val request = mutableMapOf<String, String>()
                request["username"] = username

                // Si se proporciona nueva contraseña, validar y agregar al request
                if (newPassword.isNotBlank() || confirmPassword.isNotBlank()) {
                    if (currentPassword.isBlank()) {
                        _profileState.value = ProfileState.Error("Debe proporcionar su contraseña actual")
                        return@launch
                    }
                    if (newPassword != confirmPassword) {
                        _profileState.value = ProfileState.Error("Las contraseñas no coinciden")
                        return@launch
                    }
                    request["currentPassword"] = currentPassword
                    request["newPassword"] = newPassword
                    request["confirmPassword"] = confirmPassword
                }

                val response = RetrofitClient.apiService.updateProfile(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Recargar el perfil después de actualizar
                    loadUserProfile()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _profileState.value = ProfileState.Error(
                        errorBody ?: "Error al actualizar el perfil"
                    )
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(
                    e.message ?: "Error al actualizar el perfil"
                )
            }
        }
    }

    suspend fun deleteAccount(password: String): Boolean {
        return try {
            val token = securePreferences.getToken()
            if (token == null) return false

            val response = RetrofitClient.apiService.deleteAccount("Bearer $token", password)
            if (response.success) {
                securePreferences.clearSession()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    data class Success(val userProfile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
} 