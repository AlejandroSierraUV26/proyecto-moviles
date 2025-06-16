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
    level: String,
    navController: NavController,
    viewModel: DiagnosticViewModel = viewModel()
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

    val questions by viewModel.questions.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val resultState by viewModel.resultState.collectAsState()
    val authToken by homeViewModel.authToken.collectAsState()
    Log.d("DiagnosticScreen", "AuthToken: ${authToken?.take(10)}...")

    // Resto de tu código sin cambios...
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var showResult by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Efecto para cargar preguntas al iniciar
    LaunchedEffect(Unit) {
        Log.d("DiagnosticScreen", "Cargando preguntas...")
        viewModel.loadQuestions(courseId, level)
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
        }
        DiagnosticViewModel.LoadingState.Idle -> {
            Log.d("DiagnosticScreen", "Estado idle")
        }
    }
    // Manejar resultados del diagnóstico
    LaunchedEffect(resultState) {
        when (val state = resultState) {
            is DiagnosticViewModel.ResultState.Success -> {
                val result = state.result
                navController.navigate(
                    AppScreens.DiagnosticResults.createRoute(
                        courseId = courseId,
                        level = level,
                        startingSection = result.startingSection,
                        message = result.message,
                        correctAnswers = result.correctAnswers,
                        totalQuestions = result.totalQuestions
                    )
                ) {
                    popUpTo(AppScreens.DiagnosticScreen.route) { inclusive = true }
                }
            }
            is DiagnosticViewModel.ResultState.Error -> {
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
                title = { Text("Diagnóstico - ${level.replaceFirstChar { it.uppercase() }}") },
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
                        onRetry = { viewModel.loadQuestions(courseId, level) }
                    )
                }
                is DiagnosticViewModel.LoadingState.Success -> {
                    if (questions.isNotEmpty()) {
                        // Barra de progreso
                        LinearProgressIndicator(
                            progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pregunta actual
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

                        // Controles de navegación
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
                                        viewModel.submitDiagnostic(
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
    question: Question,
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