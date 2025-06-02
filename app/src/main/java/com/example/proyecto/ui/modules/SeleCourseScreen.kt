package com.example.proyecto.ui.modules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.ui.courses.CoursesViewModel

@Composable
fun SeleCourseScreen(
    navController: NavController,
    viewModel: CoursesViewModel = viewModel()
) {
    val courses by viewModel.availableCourses.collectAsState()

    // Cargar los cursos disponibles
    LaunchedEffect(Unit) {
        viewModel.loadAvailableCourses()
    }

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
            fontWeight = FontWeight.Bold,
            color = Color(0xFF052659),
            modifier = Modifier.offset(y = (-64).dp)
        )
        Text(
            text = "¿Qué quieres \n   aprender?",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = (-38).dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(courses) { course ->
                Button(
                    onClick = {
                        navController.navigate("${AppScreens.CargaScreen.route}/${AppScreens.NivelUsuarioScreen.route}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = course.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate(AppScreens.CoursesScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .offset(y = (50).dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(
                "¡Algo nuevo!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}
