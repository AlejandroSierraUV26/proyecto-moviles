package com.example.proyecto.ui.modules

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosModuloScreen(
    examId: Int,
    navController: NavController,
    viewModel: QuizViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val examResult by viewModel.examResult.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Módulo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (loadingState) {
                is QuizViewModel.LoadingState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is QuizViewModel.LoadingState.Error -> {
                    Text(
                        text = (loadingState as QuizViewModel.LoadingState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    examResult?.let { result ->
                        // Encabezado
                        Text(
                            "Resultados del Examen",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // Puntuación
                        Text(
                            "Puntuación: ${result.correctAnswers}/${result.totalQuestions} (${result.score.toInt()}%)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                result.score >= 80 -> Color(0xFF4CAF50)  // Verde
                                result.score >= 50 -> Color(0xFFFFC107)  // Amarillo
                                else -> Color(0xFFF44336)                // Rojo
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Detalle por pregunta:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(result.feedback) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.isCorrect)
                                            Color.Green.copy(alpha = 0.1f)
                                        else
                                            Color.Red.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            item.questionText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        item.options.forEach { option ->
                                            val isSelected = option == item.selectedAnswer
                                            val isCorrect = option == item.correctAnswer

                                            Text(
                                                option,
                                                color = when {
                                                    isCorrect -> Color.Green
                                                    isSelected && !isCorrect -> Color.Red
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            "Retroalimentación: ${item.feedback}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontStyle = FontStyle.Italic
                                            ),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    } ?: run {
                        Text(
                            "No hay resultados disponibles",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
