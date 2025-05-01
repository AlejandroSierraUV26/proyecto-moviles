package com.example.proyecto.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyecto.utils.DoubleBackToExitHandler

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    
    DoubleBackToExitHandler {
        // Cierra la aplicación
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de Perfil")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
} 