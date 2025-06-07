package com.example.proyecto.ui.auth


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun RecuperScreen(navController: NavController, viewModel: PasswordRecoveryViewModel = viewModel()) {
    val viewModel: PasswordRecoveryViewModel = viewModel()

    // Efecto para navegar después de éxito
    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage == "Contraseña restablecida correctamente") {
            delay(2000) // Espera 2 segundos
            navController.navigate(AppScreens.LoginScreen.route) {
                popUpTo(AppScreens.RecuperScreen.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título dinámico según el paso
        Text(
            text = when (viewModel.currentStep) {
                1 -> "Restablece tu Contraseña"
                2 -> "Verifica tu Código"
                else -> "Crea una Nueva Contraseña"
            },
            style = TextStyle(lineHeight = 34.sp),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar mensajes
        viewModel.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            LaunchedEffect(message) {
                delay(5000)
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            LaunchedEffect(message) {
                delay(5000)
                viewModel.clearMessages()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contenido dinámico según el paso
        when (viewModel.currentStep) {
            1 -> EmailStep(viewModel)
            2 -> CodeVerificationStep(viewModel)
            3 -> NewPasswordStep(viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace para volver al login
        Text(
            text = "Volver al inicio de sesión",
            fontSize = 16.sp,
            color = Color(0xFF052659),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun EmailStep(viewModel: PasswordRecoveryViewModel) {
    Column {
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text(
                text ="Correo electrónico",
                fontSize = 16.sp
            ) },
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 14.sp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.sendRecoveryEmail() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Enviar código", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun CodeVerificationStep(viewModel: PasswordRecoveryViewModel) {
    Column {
        Text(
            text = "Ingresa el código de 4 dígitos que enviamos a ${viewModel.email}",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = viewModel.verificationCode,
            onValueChange = {
                if (it.length <= 4) {
                    viewModel.verificationCode = it
                }
            },
            label = { Text(
                text = "Código de verificación",
                fontSize = 16.sp
            ) },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.verifyCode() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Verificar código", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun NewPasswordStep(viewModel: PasswordRecoveryViewModel) {
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Campo de nueva contraseña
        OutlinedTextField(
            value = viewModel.newPassword,
            onValueChange = {
                viewModel.newPassword = it
                passwordError = if (it.isEmpty()) "Campo obligatorio" else ""
            },
            label = {
                Text(
                    text = "Nueva contraseña",
                    fontSize = 16.sp
                )
            },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = passwordError.isNotEmpty(),
            supportingText = {
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = Color.Red
                    )
                }
            },
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de confirmación de contraseña
        OutlinedTextField(
            value = viewModel.confirmPassword,
            onValueChange = {
                viewModel.confirmPassword = it
                confirmPasswordError = if (it.isEmpty()) "Campo obligatorio" else ""
            },
            label = {
                Text(
                    text = "Confirmar nueva contraseña",
                    fontSize = 16.sp
                )
            },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = confirmPasswordError.isNotEmpty(),
            supportingText = {
                if (confirmPasswordError.isNotEmpty()) {
                    Text(
                        text = confirmPasswordError,
                        color = Color.Red
                    )
                }
            },
            visualTransformation = if (showConfirmPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.newPassword.isEmpty()) {
                    passwordError = "Campo obligatorio"
                }
                if (viewModel.confirmPassword.isEmpty()) {
                    confirmPasswordError = "Campo obligatorio"
                }
                if (viewModel.newPassword.isNotEmpty() && viewModel.confirmPassword.isNotEmpty()) {
                    viewModel.resetPassword()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Restablecer contraseña", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}