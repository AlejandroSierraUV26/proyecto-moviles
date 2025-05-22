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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyecto.utils.DoubleBackToExitHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.example.proyecto.R

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Estados para los errores
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    // Estado para mostrar/ocultar contraseña
    var showPassword by remember { mutableStateOf(false) }
    
    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    DoubleBackToExitHandler {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                navController.navigate(AppScreens.HomeScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is LoginState.Error -> {
                val errorMessage = (loginState as LoginState.Error).message.lowercase()
                when {
                    errorMessage.contains("no encontrado") || errorMessage.contains("no registrado") -> {
                        emailError = "Usuario no registrado"
                        passwordError = "Usuario no registrado"
                    }
                    errorMessage.contains("credenciales") || errorMessage.contains("inválidas") -> {
                        emailError = "Su usuario/correo o contraseña son erróneos"
                        passwordError = "Su usuario/correo o contraseña son erróneos"
                    }
                    else -> {
                        emailError = "Error al iniciar sesión"
                        passwordError = "Error al iniciar sesión"
                    }
                }
            }
            else -> {}
        }
    }

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

        // Campo de correo/usuario
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                emailError = if (it.isEmpty()) "Campo obligatorio" else ""
            },
            label = {
                Text(
                    text = "Correo o Usuario",
                    fontSize = 16.sp)
            },
            textStyle = TextStyle(fontSize = 14.sp), // ← Tamaño más pequeño dentro del input
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = emailError.isNotEmpty(),
            supportingText = {
                if (emailError.isNotEmpty()) {
                    Text(
                        text = emailError,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = if (it.isEmpty()) "Campo obligatorio" else ""
            },
            label = {
                Text(
                    text = "Contraseña",
                    fontSize = 16.sp)
            },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier
                .fillMaxWidth(),
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
                androidx.compose.ui.text.input.VisualTransformation.None
            } else {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(
                            id = if (showPassword) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Se te olvidó la contraseña?",
            fontSize = 14.sp,
            color = Color(0xFF5678C1),
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { navController.navigate(AppScreens.RecuperScreen.route)}
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de ingresar
        Button(
            onClick = {
                // Limpiar errores previos
                emailError = ""
                passwordError = ""
                
                // Validar campos antes de intentar login
                var hasError = false
                
                if (email.isEmpty()) {
                    emailError = "Campo obligatorio"
                    hasError = true
                }
                
                if (password.isEmpty()) {
                    passwordError = "Campo obligatorio"
                    hasError = true
                }
                
                if (!hasError) {
                    viewModel.loginUser(email, password)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            enabled = loginState !is LoginState.Loading
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Ingresar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
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
                navController.navigate(AppScreens.RegisterScreen.route) {
                    popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50)
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