package com.example.proyecto.ui.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.navigation.AppScreens


@Composable
fun SeleCourseScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "KnowlT",
            fontSize = 30.sp,
            fontWeight = FontWeight.Companion.Bold,
            color = Color(0xFF052659),
            modifier = Modifier.Companion.offset(y = (-64).dp)
        )
        Text(
            text = "¿Qué quieres \n   aprender?",
            fontSize = 25.sp,
            fontWeight = FontWeight.Companion.Bold,
            modifier = Modifier.offset(y = (-38).dp)
        )

        Spacer(modifier = Modifier.Companion.height(32.dp))
        // 🔵 Aquí cambiamos: SOLO los cursos en grid
        val cursos = listOf("Matemáticas", "Ciencia", "Historia", "Sociales")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(350.dp), // puedes ajustar esta altura
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cursos) { curso ->
                Button(
                    onClick = { navController.navigate("${AppScreens.CargaScreen.route}/${AppScreens.NivelUsuarioScreen.route}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = curso,
                        color = Color.Companion.White,
                        fontWeight = FontWeight.Companion.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 🔵 Botón "Algo nuevo" normal
        Button(
            onClick = { /* Navegar a algo nuevo */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .offset(y = (50).dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
        ) {
            Text(
                "¡Algo nuevo!",
                color = Color.Companion.White,
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeleCourseScreenPreview() {
    val navController = rememberNavController()
    SeleCourseScreen(navController = navController)
}