package com.example.proyecto.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens

@Composable
fun ResultadosModuloScreen(
    quizViewModel: QuizViewModel,
    navController: NavController
) {
    val aciertos = quizViewModel.aciertos.collectAsState()
    val progreso = quizViewModel.progreso.collectAsState()
    val recomendaciones = quizViewModel.recomendaciones.collectAsState()

    // Y luego usas esos valores normalmente en tu UI:
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Completaste el \nmódulo!",
            style = TextStyle(lineHeight = 34.sp),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoBox(label = "Aciertos", value = "$aciertos")
            InfoBox(label = "Progreso", value = "$progreso%")
        }

        Column(modifier = Modifier.fillMaxWidth()) {

            Text(
                text = "Recomendaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .border(
                        width = 3.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.White),
            ) {
                Text(text = recomendaciones.value,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(12.dp))
            }
        }

        Button(
            onClick = { 
                navController.navigate(AppScreens.HomeScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
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
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun InfoBox(label: String, value: String) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
    ) {
        // Etiqueta encima
        Text(
            fontSize = 18.sp,
            text = label,
            fontWeight = FontWeight.Bold,
            color = textColor // <- adaptado al tema
        )

        // Caja visual
        Box(
            modifier = Modifier
                .padding(top = 19.dp)
                .height(60.dp)
                .width(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 3.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor // <- también aquí
            )
        }
    }
}
