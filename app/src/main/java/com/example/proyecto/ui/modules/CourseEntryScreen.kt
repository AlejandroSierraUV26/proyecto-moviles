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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.navigation.AppScreens
import kotlin.collections.listOf

@Composable
fun CourseEntryScreen(navController: NavController) {

    val levels = listOf("Desde el principio", "Hacer un quiz")
    var selectedLevel by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Text(
                text = "¿Desde donde \nempezar?",
                style = TextStyle(lineHeight = 34.sp),
                modifier = Modifier.
                offset(y = (-42).dp)
                    .padding(horizontal = 50.dp),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))

            levels.forEach { level ->
                val isSelected = selectedLevel == level
                val isDark = isSystemInDarkTheme()

                val borderColor =
                    if (isSelected) Color(0xFF163DA8) else Color.Gray.copy(alpha = 0.5f)
                val backgroundColor = if (isSelected) Color(0xFF95A3F3) else Color.Transparent
                val textColor = if (isSelected) {
                    Color(0xFF052659) // Color del texto seleccionado
                } else {
                    if (isDark) Color.White else Color.Black
                }

                OutlinedButton(
                    onClick = { selectedLevel = level }, // ✅ Selecciona el nivel
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
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
                    )
                ) {
                    Text(
                        text = level,
                        color = textColor, // ✅ Ahora usa el color dinámico
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // 🔵 Botón "Algo nuevo" normal
        Button(
            onClick = {
                navController.navigate("${AppScreens.CargaScreen.route}/${AppScreens.SetPreguntasSreen.route}")
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
                fontSize = 20.sp
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CourseEntryScreenPreview() {
    val navController = rememberNavController()
    CourseEntryScreen(navController = navController)
}
