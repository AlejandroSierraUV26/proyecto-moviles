package com.example.proyecto.ui.modules

import android.app.Application
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.proyecto.data.models.DiagnosticQuestion
import com.example.proyecto.data.models.Question
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.data.models.DiagnosticResult
import com.example.proyecto.ui.home.HomeViewModel
import com.example.proyecto.ui.home.HomeViewModelFactory
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    courseId: Int,
    level: Int,
    navController: NavController,
    diagnosticViewModel: DiagnosticViewModel = viewModel(factory = DiagnosticViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    Log.d("DiagnosticScreen", "courseId: $courseId, level: $level")
    Log.d("DiagnosticScreen", "Iniciando pantalla de diagnóstico para courseId: $courseId, level: $level")


    val context = LocalContext.current
    val application = context.applicationContext as Application
    val backStackEntry = navController.currentBackStackEntryAsState().value

    // Instanciar HomeViewModel correctamente
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application)
    )

    val questions by diagnosticViewModel.questions.collectAsState()
    val loadingState by diagnosticViewModel.loadingState.collectAsState()
    val resultState by diagnosticViewModel.resultState.collectAsState()
    val authToken by homeViewModel.authToken.collectAsState()
    Log.d("DiagnosticScreen", "Token obtenido de HomeViewModel: ${authToken?.take(10)}... (longitud: ${authToken?.length})")

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var showResult by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Convertir nivel numérico a texto para mostrar en UI
    val levelName = remember(level) {
        when (level) {
            1 -> "Básico"
            2 -> "Intermedio"
            3 -> "Avanzado"
            else -> "Nivel $level"
        }
    }

    // Establecer el token en el DiagnosticViewModel
    LaunchedEffect(authToken) {
        authToken?.let { token ->
            Log.d("DiagnosticScreen", "Estableciendo token en DiagnosticViewModel")
            diagnosticViewModel.setAuthToken(token)
            diagnosticViewModel.loadQuestions(courseId, level)
        }?: run {
            Log.e("DiagnosticScreen", "No se encontró token de autenticación")
        }
    }


    // Log para estado de carga
    when (loadingState) {
        is DiagnosticViewModel.LoadingState.Loading -> {
            Log.d("DiagnosticScreen", "Cargando preguntas...")
        }
        is DiagnosticViewModel.LoadingState.Error -> {
            Log.e("DiagnosticScreen", "Error al cargar preguntas: ${(loadingState as DiagnosticViewModel.LoadingState.Error).message}")
        }
        is DiagnosticViewModel.LoadingState.Success -> {
            Log.d("DiagnosticScreen", "Preguntas cargadas: ${questions.size}")
            Log.d("DiagnosticScreen", "Primera pregunta: ${questions.firstOrNull()?.questionText?.take(30)}...")
        }
        DiagnosticViewModel.LoadingState.Idle -> {
            Log.d("DiagnosticScreen", "Estado idle")
        }
    }

    // Log para respuestas seleccionadas
    LaunchedEffect(selectedAnswers) {
        Log.d("DiagnosticScreen", "Respuestas seleccionadas actualizadas: $selectedAnswers")
    }


    LaunchedEffect(resultState) {
        when (val state = resultState) {
            is DiagnosticViewModel.ResultState.Success -> {
                val result = state.result
                Log.d("DiagnosticScreen", "Resultado exitoso: $result")
                navController.navigate(
                    AppScreens.DiagnosticResults.createRoute(
                        levelTested = result.levelTested, // Usamos el levelTested del resultado
                        passed = result.passed,
                        score = result.score,
                        startingSection = URLEncoder.encode(result.startingSection, "UTF-8"),
                        message = URLEncoder.encode(result.message, "UTF-8")
                    )
                ) {
                    popUpTo(AppScreens.DiagnosticScreen.route) { inclusive = true }
                }
            }
            is DiagnosticViewModel.ResultState.Error -> {
                Log.e("DiagnosticScreen", "Error en el resultado: ${state.message}")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico - $levelName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (loadingState) {
                is DiagnosticViewModel.LoadingState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DiagnosticViewModel.LoadingState.Error -> {
                    ErrorRetryView(
                        error = (loadingState as DiagnosticViewModel.LoadingState.Error).message,
                        onRetry = { diagnosticViewModel.loadQuestions(courseId, level) }
                    )
                }
                is DiagnosticViewModel.LoadingState.Success -> {
                    if (questions.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        QuestionItem(
                            question = questions[currentQuestionIndex],
                            selectedAnswer = selectedAnswers[questions[currentQuestionIndex].id],
                            showResult = showResult,
                            onAnswerSelected = { answer ->
                                selectedAnswers = selectedAnswers +
                                        (questions[currentQuestionIndex].id to answer)
                                showResult = true
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    currentQuestionIndex--
                                    showResult = false
                                },
                                enabled = currentQuestionIndex > 0
                            ) {
                                Text("Anterior")
                            }

                            if (currentQuestionIndex < questions.size - 1) {
                                Button(
                                    onClick = {
                                        currentQuestionIndex++
                                        showResult = false
                                    },
                                    enabled = selectedAnswers.containsKey(questions[currentQuestionIndex].id)
                                ) {
                                    Text("Siguiente")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        diagnosticViewModel.submitDiagnostic(
                                            courseId = courseId,
                                            level = level,
                                            answers = selectedAnswers,
                                            authToken = authToken!!
                                        )
                                    },
                                    enabled = selectedAnswers.containsKey(questions[currentQuestionIndex].id)
                                ) {
                                    Text("Finalizar")
                                }
                            }
                        }
                    } else {
                        EmptyQuestionsView()
                    }
                }
                DiagnosticViewModel.LoadingState.Idle -> {}
            }
        }
    }
}

@Composable
private fun ErrorRetryView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
            modifier = Modifier
                .height(50.dp)
                .width(120.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                "Reintentar",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyQuestionsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = "Sin preguntas",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF052659)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No hay preguntas disponibles para este nivel",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun QuestionItem(
    question: DiagnosticQuestion,
    selectedAnswer: String?,
    showResult: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question.questionText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        question.options.forEach { option ->
            val isCorrect = option == question.correctAnswer
            val isSelected = option == selectedAnswer

            val borderColor = when {
                showResult && isCorrect -> Color(0xFF4CAF50)
                showResult && isSelected && !isCorrect -> Color(0xFFF44336)
                isSelected -> Color(0xFF163DA8)
                else -> Color.Gray.copy(alpha = 0.5f)
            }

            val backgroundColor = when {
                showResult && isCorrect -> Color(0xFFA5D6A7)
                showResult && isSelected && !isCorrect -> Color(0xFFEF9A9A)
                isSelected -> Color(0xFF95A3F3)
                else -> Color.Transparent
            }

            val textColor = when {
                showResult && isCorrect -> Color(0xFF1B5E20)
                showResult && isSelected && !isCorrect -> Color(0xFFB71C1C)
                isSelected -> Color(0xFF052659)
                else -> if (isSystemInDarkTheme()) Color.White else Color.Black
            }

            OutlinedButton(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(width = 2.dp, color = borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = backgroundColor,
                    contentColor = textColor
                ),
                enabled = !showResult
            ) {
                Text(
                    text = option,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}