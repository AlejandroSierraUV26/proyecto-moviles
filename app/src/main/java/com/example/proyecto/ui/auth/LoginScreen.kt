package com.example.proyecto.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Inicia Sesión!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    text = "Correo o usuario",
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(28),
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    text = "Contraseña",
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(28),
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Se te olvidó la contraseña?",
            fontSize = 14.sp,
            color = Color(0xFF5678C1),
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { /* Aquí iría navegación o acción */ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de ingresar
        Button(
            onClick = {
                // Aquí va la lógica de autenticación
                navController.navigate(AppScreens.HomeScreen.route) {
                    // Limpia el historial de navegación
                    popUpTo(0) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("Ingresar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿No tienes cuenta?",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp 
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(AppScreens.RegisterScreen.route)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("Regístrate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}