package com.example.proyecto.ui.modules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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


// Modelo de pregunta
data class Pregunta(
    val texto: String,
    val opciones: List<String>,
    val respuestaCorrecta: String
)

@Composable
fun SetPreguntasScreen(
    preguntas: List<Pregunta>,
    onQuizFinalizado: () -> Unit,
    navController: NavController,
    quizViewModel: QuizViewModel = viewModel()
) {
    var indicePregunta by remember { mutableStateOf(0) }
    var respuestaSeleccionada by remember { mutableStateOf<String?>(null) }
    var mostrarResultado by remember { mutableStateOf(false) }
    var aciertos by remember { mutableStateOf(0) }

    val preguntaActual = preguntas[indicePregunta]
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .offset(y = (55).dp)
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = preguntaActual.texto,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        preguntaActual.opciones.forEach { opcion ->
            val esCorrecta = opcion == preguntaActual.respuestaCorrecta
            val fueSeleccionada = opcion == respuestaSeleccionada
            val isSelected = fueSeleccionada

            val borderColor = when {
                mostrarResultado && esCorrecta -> Color(0xFF4CAF50) // Verde para correcta
                mostrarResultado && fueSeleccionada -> Color(0xFFF44336) // Rojo para incorrecta seleccionada
                isSelected -> Color(0xFF163DA8) // Azul para seleccionada
                else -> Color.Gray.copy(alpha = 0.5f)
            }

            val backgroundColor = when {
                mostrarResultado && esCorrecta -> Color(0xFFA5D6A7) // Verde claro
                mostrarResultado && fueSeleccionada -> Color(0xFFEF9A9A) // Rojo claro
                isSelected -> Color(0xFF95A3F3) // Azul claro
                else -> Color.Transparent
            }

            val textColor = when {
                mostrarResultado && esCorrecta -> Color(0xFF1B5E20) // Verde oscuro
                mostrarResultado && fueSeleccionada -> Color(0xFFB71C1C) // Rojo oscuro
                isSelected -> Color(0xFF052659) // Azul oscuro
                else -> if (isDark) Color.White else Color.Black
            }

            OutlinedButton(
                onClick = {
                    if (!mostrarResultado) {
                        respuestaSeleccionada = opcion
                        mostrarResultado = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .size(72.dp)
                    .offset(y = 42.dp),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 3.dp,
                    color = borderColor
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = backgroundColor,
                    contentColor = textColor
                ),
                enabled = !mostrarResultado // evitar cambios después de mostrar resultado
            ) {
                Text(
                    text = opcion,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))


        Button(
            onClick = {
                if (respuestaSeleccionada == preguntaActual.respuestaCorrecta) {
                    aciertos++
                }

                if (indicePregunta < preguntas.size - 1) {
                    indicePregunta++
                    respuestaSeleccionada = null
                    mostrarResultado = false
                } else {
                    val progreso = ((aciertos.toFloat() / preguntas.size) * 100).toInt()
                    val recomendacion = if (progreso < 100) {
                        "Repasa los conceptos que no acertaste y vuelve a intentarlo."
                    } else {
                        "¡Excelente! Puedes continuar con el siguiente módulo."
                    }

                    quizViewModel.setResultados(aciertos, progreso, recomendacion)
                    navController.navigate(AppScreens.ResultadosModuloScreen.route)
                }
            },
            enabled = mostrarResultado,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .offset(y = (50).dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
        ) {
            Text(
                "Continuar",
                color = Color.Companion.White,
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 20.sp
            )
        }
    }
}