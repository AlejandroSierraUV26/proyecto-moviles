package com.example.proyecto.ui.modules

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.proyecto.data.models.DiagnosticResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticResultsScreen(
    navController: NavController,
    viewModel: DiagnosticResultsViewModel
) {

    // Procesar el feedbackJson si viene en los argumentos
    val feedbackJson = remember { navController.currentBackStackEntry?.arguments?.getString("feedbackJson") }

    // Estados del ViewModel
    val results by viewModel.results.collectAsState()
    val overallResult by viewModel.overallResult.collectAsState()
    val recommendedSection by viewModel.recommendedSection.collectAsState()
    val averageScore by viewModel.averageScore.collectAsState()
    val passed by viewModel.passed.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()


    LaunchedEffect(feedbackJson) {
        if (feedbackJson != null) {
            viewModel.processFeedbackJson(feedbackJson)
        }
    }

    Log.d("ResultsScreen", "Feedback JSON recibido: $feedbackJson")

    if (feedbackJson == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se recibieron datos de resultados", color = Color.Red)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Diagnóstico") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally  // Centrar toda la columna
            ) {
                // Resultado general
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)  // Reducir un poco el ancho para mejor aspecto
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Resultado General",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Indicador de progreso circular - Modificado
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(120.dp)  // Tamaño reducido
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${averageScore.toInt()}%",
                                    style = MaterialTheme.typography.displaySmall.copy(  // Tamaño reducido
                                        fontWeight = FontWeight.Bold,
                                        color = if (passed) Color.Green else Color.Red
                                    )
                                )
                                Text(
                                    text = if (passed) "APROBADO" else "NO APROBADO",
                                    color = if (passed) Color.Green else Color.Red,
                                    style = MaterialTheme.typography.titleMedium,  // Tamaño reducido
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = overallResult,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Resultados por nivel
                Text(
                    text = "Resultados por Nivel",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp, 8.dp)
                )

                results.forEach { result ->
                    DiagnosticLevelResultCard(result = result)
                }

                // Recomendación
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Recomendación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sección recomendada para comenzar:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = recommendedSection,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DiagnosticLevelResultCard(result: DiagnosticResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)  // Reducir un poco el ancho
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed)
                Color.Green.copy(alpha = 0.1f)
            else
                Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nivel ${result.levelTested} - ${when (result.levelTested) {
                        1 -> "Básico"
                        2 -> "Intermedio"
                        3 -> "Avanzado"
                        else -> ""
                    }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (result.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.passed) Color.Green else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(12.dp))  // Más espacio antes de la barra

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((result.score / 100f).toFloat())
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (result.passed) Color.Green else Color.Red)
                )

                Text(
                    text = "${result.score.toInt()}%",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall  // Texto más pequeño
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Puntuación: ${"%.1f".format(result.score)}%",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp)  // Tamaño reducido
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.message ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}