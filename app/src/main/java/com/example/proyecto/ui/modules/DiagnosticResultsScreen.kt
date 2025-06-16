package com.example.proyecto.ui.modules

import androidx.compose.foundation.layout.*
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
    courseId: Int,
    level: String,
    startingSection: String,
    message: String,
    correctAnswers: Int,
    totalQuestions: Int,
    navController: NavController
) {
    val percentage = (correctAnswers.toFloat() / totalQuestions * 100).toInt()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.size(150.dp)
        )

        Text("$percentage%", style = MaterialTheme.typography.headlineLarge)
        Text("Correctas: $correctAnswers/$totalQuestions")
        Text("Nivel: ${level.replaceFirstChar { it.uppercase() }}")
        Text("Sección recomendada: $startingSection")
        Text(message, textAlign = TextAlign.Center)

        Button(
            onClick = {
                navController.navigate(AppScreens.HomeScreen.route) {
                    popUpTo(0)
                }
            }
        ) {
            Text("Ir al Inicio")
        }
    }
}