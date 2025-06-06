package com.example.proyecto.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.api.ApiService
import com.example.proyecto.data.api.RetrofitClient
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ProfileViewModel"
    
    var profileImageUrl by mutableStateOf<String?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadProfileImage()
    }

    fun loadProfileImage() {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                Log.d(TAG, "Cargando imagen de perfil...")
                val response = RetrofitClient.apiService.getProfileImage()
                if (response.success) {
                    profileImageUrl = response.imageUrl
                    Log.d(TAG, "Imagen cargada exitosamente: $profileImageUrl")
                } else {
                    Log.e(TAG, "Error al cargar imagen: ${response.message}")
                    error = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar imagen de perfil", e)
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                Log.d(TAG, "Actualizando imagen de perfil...")
                
                val imagePart = ApiService.createImagePart(imageUri, getApplication())
                Log.d(TAG, "Parte de imagen creada: ${imagePart.body?.contentType()}")
                
                val response = RetrofitClient.apiService.updateProfileImage(imagePart)
                if (response.success) {
                    profileImageUrl = response.imageUrl
                    Log.d(TAG, "Imagen actualizada exitosamente: $profileImageUrl")
                } else {
                    Log.e(TAG, "Error al actualizar imagen: ${response.message}")
                    error = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar imagen de perfil", e)
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                Log.d(TAG, "Eliminando imagen de perfil...")
                
                val response = RetrofitClient.apiService.deleteProfileImage()
                if (response.success) {
                    profileImageUrl = null
                    Log.d(TAG, "Imagen eliminada exitosamente")
                } else {
                    Log.e(TAG, "Error al eliminar imagen: ${response.message}")
                    error = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar imagen de perfil", e)
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
} 