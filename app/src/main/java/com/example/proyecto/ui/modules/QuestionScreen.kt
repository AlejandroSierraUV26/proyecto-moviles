package com.example.proyecto.ui.modules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.data.models.AnswerSubmission
import com.example.proyecto.ui.modules.ResultadosViewModel

@Composable
fun QuestionScreen(
    examId: Int,
    navController: NavController,
    quizViewModel: QuizViewModel = viewModel(),
    resultadosViewModel: ResultadosViewModel = viewModel()
) {
    val questions by quizViewModel.questions.collectAsState()
    val loadingState by quizViewModel.loadingState.collectAsState()

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    val selectedAnswers = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(Unit) {
        quizViewModel.loadQuestions(examId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .offset(y = 55.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (loadingState) {
            is QuizViewModel.LoadingState.Loading -> {
                CircularProgressIndicator()
            }

            is QuizViewModel.LoadingState.Error -> {
                Text(
                    text = "Error: ${(loadingState as QuizViewModel.LoadingState.Error).message}",
                    color = Color.Red
                )
            }

            is QuizViewModel.LoadingState.Idle -> {
                if (questions.isNotEmpty()) {
                    val question = questions[currentQuestionIndex]

                    Text(
                        text = question.questionText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    question.options.forEach { option ->
                        val isCorrect = option == question.correctAnswer
                        val isSelected = option == selectedAnswer

                        val borderColor = when {
                            showResult && isCorrect -> Color(0xFF4CAF50)
                            showResult && isSelected -> Color(0xFFF44336)
                            isSelected -> Color(0xFF163DA8)
                            else -> Color.Gray.copy(alpha = 0.5f)
                        }

                        val backgroundColor = when {
                            showResult && isCorrect -> Color(0xFFA5D6A7)
                            showResult && isSelected -> Color(0xFFEF9A9A)
                            isSelected -> Color(0xFF95A3F3)
                            else -> Color.Transparent
                        }

                        val textColor = when {
                            showResult && isCorrect -> Color(0xFF1B5E20)
                            showResult && isSelected -> Color(0xFFB71C1C)
                            isSelected -> Color(0xFF052659)
                            else -> if (isSystemInDarkTheme()) Color.White else Color.Black
                        }

                        OutlinedButton(
                            onClick = {
                                if (!showResult) {
                                    selectedAnswer = option
                                    showResult = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .size(72.dp)
                                .offset(y = 42.dp),
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

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (selectedAnswer != null) {
                                selectedAnswers[question.id] = selectedAnswer!!

                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswer = selectedAnswers[questions[currentQuestionIndex].id]
                                    showResult = false
                                } else {
                                    // Construimos la lista de respuestas a enviar
                                    val answersList = questions.map { q ->
                                        AnswerSubmission(
                                            questionId = q.id,
                                            selectedAnswer = selectedAnswers[q.id] ?: ""
                                        )
                                    }

                                    // Guardamos las respuestas en ambos ViewModels
                                    quizViewModel.setAllAnswers(answersList)
                                    resultadosViewModel.setUserAnswers(answersList)


                                    // Navegar a resultados
                                    quizViewModel.setAllAnswers(answersList)
                                    navController.navigate("${AppScreens.ResultadosModuloScreen.route}/$examId")
                                }
                            }
                        },
                        enabled = showResult,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .offset(y = 50.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Text(
                            "Continuar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                } else {
                    Text("No hay preguntas disponibles.")
                }
            }
        }
    }
}
