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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.serialization.json.Json
import com.example.proyecto.data.models.AnswerFeedback
import com.example.proyecto.data.models.ExamFeedbackResult




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosModuloScreen(
    examId: Int,
    navController: NavController,
    viewModel: ResultadosViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val examResult by viewModel.examResult.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    var examSubmitted by remember { mutableStateOf(false) }
    val userAnswers by viewModel.userAnswers.collectAsState()
    val feedbackResult by viewModel.examFeedback.collectAsState()
    val feedbackList: List<AnswerFeedback> = feedbackResult?.feedbackList ?: emptyList()


    LaunchedEffect(Unit) {
        if (!examSubmitted && userAnswers.isNotEmpty()) {
            viewModel.submitExam(examId, userAnswers)
            examSubmitted = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Examen") },
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
                is ResultadosViewModel.LoadingState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is ResultadosViewModel.LoadingState.Error -> {
                    Text(
                        text = (loadingState as ResultadosViewModel.LoadingState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    examResult?.let { result ->

                        // NUEVO BLOQUE DE PUNTUACIÓN Y PORCENTAJE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Puntuación", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Card(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .width(80.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                                )
                                {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),  // o un tamaño fijo, ej: .width(80.dp)
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${result.correctAnswers}/${result.totalQuestions}",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Porcentaje", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Card(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .width(80.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),  // o un tamaño fijo, ej: .width(80.dp)
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${result.percentage}%",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Detalle por pregunta:",
                            style = MaterialTheme.typography.titleMedium
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(feedbackList) { item: AnswerFeedback ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.isCorrect)
                                            Color.Green.copy(alpha = 0.1f)
                                        else
                                            Color.Red.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = item.questionText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 20.sp,
                                                lineHeight = 24.sp
                                            ),
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        item.options.forEach { option ->
                                            val isSelected = option == item.selectedAnswer
                                            val isCorrect = option == item.correctAnswer

                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 20.sp,
                                                    lineHeight = 24.sp
                                                ),
                                                color = when {
                                                    isCorrect -> Color.Green
                                                    isSelected && !isCorrect -> Color.Red
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            "Respuesta correcta: ${item.correctAnswer}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 20.sp,
                                                lineHeight = 24.sp
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