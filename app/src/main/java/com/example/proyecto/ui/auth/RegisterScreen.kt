package com.example.proyecto.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyecto.utils.DoubleBackToExitHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.proyecto.R

@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Estados para los errores
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    // Estado para mostrar/ocultar contraseña
    var showPassword by remember { mutableStateOf(false) }
    
    val viewModel: RegisterViewModel = viewModel()
    val registerState by viewModel.registerState.collectAsStateWithLifecycle()

    DoubleBackToExitHandler {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    // Función para validar el formato del correo
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                navController.navigate(AppScreens.SeleCourseScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is RegisterState.Error -> {
                val errorMessage = (registerState as RegisterState.Error).message.lowercase()
                when {
                    errorMessage.contains("username") || errorMessage.contains("usuario") -> {
                        usernameError = "Este nombre de usuario ya está en uso"
                    }
                    errorMessage.contains("email") || errorMessage.contains("correo") -> {
                        emailError = "Este correo electrónico ya está registrado"
                    }
                    else -> {
                        // Para otros errores, mostrarlos en el campo correspondiente
                        if (errorMessage.contains("password") || errorMessage.contains("contraseña")) {
                            passwordError = "Error en la contraseña"
                        } else {
                            // Si no podemos determinar el campo específico, mostramos el error en el correo
                            emailError = "Error al registrar: ${(registerState as RegisterState.Error).message}"
                        }
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
            text = "¡Regístrate!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de correo
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                emailError = when {
                    it.isEmpty() -> "El correo es obligatorio"
                    !isValidEmail(it) -> "Formato de correo inválido"
                    else -> ""
                }
            },
            label = {
                Text(
                    text = "Correo",
                    fontSize = 16.sp)
            },
            textStyle = TextStyle(fontSize = 14.sp),
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

        // Campo de usuario
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                usernameError = if (it.isEmpty()) "El usuario es obligatorio" else ""
            },
            label = {
                Text(
                    text = "Usuario",
                    fontSize = 16.sp)
            },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = usernameError.isNotEmpty(),
            supportingText = {
                if (usernameError.isNotEmpty()) {
                    Text(
                        text = usernameError,
                        color = Color.Red
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = if (it.isEmpty()) "La contraseña es obligatoria" else ""
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

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de crear cuenta
        Button(
            onClick = {
                // Limpiar errores previos
                emailError = ""
                usernameError = ""
                passwordError = ""
                
                // Validar campos antes de registrar
                var hasError = false
                
                // Validar correo
                if (email.isEmpty()) {
                    emailError = "El correo es obligatorio"
                    hasError = true
                } else if (!isValidEmail(email)) {
                    emailError = "Formato de correo inválido"
                    hasError = true
                }
                
                // Validar usuario
                if (username.isEmpty()) {
                    usernameError = "El usuario es obligatorio"
                    hasError = true
                }
                
                // Validar contraseña
                if (password.isEmpty()) {
                    passwordError = "La contraseña es obligatoria"
                    hasError = true
                }
                
                if (!hasError) {
                    viewModel.registerUser(email, username, password)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF052659)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            enabled = registerState !is RegisterState.Loading
        ) {
            if (registerState is RegisterState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Crear Cuenta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Ya tienes cuenta?",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(AppScreens.RegisterScreen.route) { inclusive = true }
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
            Text("Ingresa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}