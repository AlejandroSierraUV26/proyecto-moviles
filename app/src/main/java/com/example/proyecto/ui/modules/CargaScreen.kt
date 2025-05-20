package com.example.proyecto.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.proyecto.navigation.AppScreens

@Composable
fun CargaScreen(
    navController: NavController?,
    destinationRoute: String,
    delayMillis: Long = 1000L
) {
    if (navController != null) {
        LaunchedEffect(Unit) {
            delay(delayMillis)
            navController.navigate(destinationRoute) {
                popUpTo(AppScreens.CargaScreen.route) { inclusive = true } // Opcional: evita que vuelva atrás
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.LightGray, RoundedCornerShape(50))
                )
            }
        }
    }
}
