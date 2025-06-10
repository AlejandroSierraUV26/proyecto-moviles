package com.example.proyecto

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.navigation.AppNavigation
import com.example.proyecto.ui.theme.ProyectoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.initialize(this)
        setContent {
            ProyectoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ){
                    AppNavigation()
                }
            }
        }
        // En tu Application class o primera Activity:
        if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            try {
                val settings = Settings.Global.getInt(contentResolver, "force_gpu_rendering", 0)
                if (settings != 1) {
                    Toast.makeText(this, "Ajusta configuración GPU en opciones de desarrollador", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MIUI_FIX", "Error checking GPU settings", e)
            }
        }
    }
}