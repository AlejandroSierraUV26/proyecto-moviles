package com.example.proyecto.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.ui.theme.ProyectoTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    ProyectoTheme {
        LaunchedEffect(key1 = true) {
            delay(2000)
            navController.popBackStack()
            navController.navigate(AppScreens.LoginScreen.route)
        }

        Splash()
    }

}

@Composable
fun Splash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(red = 5, green = 38, blue = 89),
                        Color(red = 1, green = 16, blue = 35)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KnowIT",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Aprende lo que \nquieras!",
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 36.sp),
                color = Color.White,
                textAlign = TextAlign.Center
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    ProyectoTheme {
        Splash()
    }
}
