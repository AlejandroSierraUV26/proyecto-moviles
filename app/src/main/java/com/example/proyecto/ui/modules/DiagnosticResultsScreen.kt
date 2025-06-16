package com.example.proyecto.ui.modules

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.ui.home.HomeViewModel


@Composable
fun DiagnosticResultsScreen(
    levelTested: Int,
    passed: Boolean,
    score: Double,
    startingSection: String,
    message: String,
    navController: NavController
) {

    Log.d("DiagnosticResults", "Mostrando resultados:")
    Log.d("DiagnosticResults", " - Nivel evaluado: $levelTested")
    Log.d("DiagnosticResults", " - Aprobado: $passed")
    Log.d("DiagnosticResults", " - Puntaje: $score")
    Log.d("DiagnosticResults", " - Sección recomendada: $startingSection")
    Log.d("DiagnosticResults", " - Mensaje: $message")

    val percentage = (score * 100).toInt()
    val levelName = when(levelTested) {
        1 -> "Básico"
        2 -> "Intermedio"
        3 -> "Avanzado"
        else -> "Nivel $levelTested"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar icono según si pasó o no
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = "Resultado",
            tint = if (passed) Color.Green else Color.Red,
            modifier = Modifier.size(100.dp))

        Text("$percentage%", style = MaterialTheme.typography.headlineLarge)
        Text("Nivel: $levelName")
        Text("Sección recomendada: $startingSection")
        Text(message, textAlign = TextAlign.Center)

        Button(
            onClick = { navController.navigate(AppScreens.HomeScreen.route) }
        ) {
            Text("Continuar")
        }
    }
}