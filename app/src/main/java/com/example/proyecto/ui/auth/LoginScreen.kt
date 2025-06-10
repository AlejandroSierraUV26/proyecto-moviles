package com.example.proyecto.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyecto.utils.DoubleBackToExitHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.proyecto.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// En tu ViewModel o clase de configuración
private const val ANDROID_CLIENT_ID = "968858227331-340cl67hmbnmf0ov6058hg9sarp1bi8k.apps.googleusercontent.com"

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }

    val viewModel: LoginViewModel = viewModel()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var googleIdToken by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Configuración del lanzador de Google Sign-In

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)

                    account.idToken?.let { token ->
                        Log.d("GOOGLE_AUTH", "Token length: ${token.length}")
                        viewModel.loginWithGoogle(token)
                    } ?: run {
                        Log.e("GOOGLE_AUTH", "Token is null")
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: No se pudo obtener token de Google")
                        }
                    }
                } catch (e: ApiException) {
                    Log.e("GOOGLE_AUTH", "SignInResult: failed code=${e.statusCode}", e)
                    scope.launch {
                        snackbarHostState.showSnackbar("Error en autenticación: ${e.statusCode}")
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.w("GOOGLE_AUTH", "SignInResult: canceled")
                scope.launch {
                    snackbarHostState.showSnackbar("Autenticación cancelada")
                }

                // Depuración adicional:
                result.data?.extras?.keySet()?.forEach { key ->
                    Log.d("GOOGLE_AUTH_DEBUG", "Extra key: $key, value: ${result.data?.extras?.get(key)}")
                }
            }
            else -> {
                Log.e("GOOGLE_AUTH", "SignInResult: unknown result code ${result.resultCode}")
                scope.launch {
                    snackbarHostState.showSnackbar("Error desconocido: ${result.resultCode}")
                }
            }
        }
    }

    LaunchedEffect(googleIdToken) {
        googleIdToken?.let { token ->
            Log.d("GOOGLE_AUTH", "🚀 Token recibido en LaunchedEffect. Iniciando autenticación...")
            viewModel.loginWithGoogle(token)
            delay(500) // Pequeña pausa si necesario
            googleIdToken = null
        }
    }


// En tu Activity/Composable
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(ANDROID_CLIENT_ID) // Client ID de Android
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    DoubleBackToExitHandler {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun verifyGoogleSignInConfig(context: Context) {
        try {
            val options = GoogleSignInOptions.DEFAULT_SIGN_IN
            val account = GoogleSignIn.getLastSignedInAccount(context)

            if (account == null) {
                Log.d("GOOGLE_AUTH", "No hay cuenta registrada")
            } else {
                Log.d("GOOGLE_AUTH", "Cuenta: ${account.email}")
            }

            val apiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
            if (resultCode != ConnectionResult.SUCCESS) {
                Log.e("GOOGLE_AUTH", "Google Play Services no disponibles")
            }
        } catch (e: Exception) {
            Log.e("GOOGLE_AUTH", "Error en configuración", e)
        }
    }

    // Manejo del estado del login
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
                        emailError = "Correo o contraseña erróneos"
                        passwordError = "Correo o contraseña erróneos"
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

        // Campos de email y contraseña (se mantienen igual)
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (it.isEmpty()) "Campo obligatorio" else ""
            },
            label = { Text("Correo o Usuario", fontSize = 16.sp) },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = emailError.isNotEmpty(),
            supportingText = {
                if (emailError.isNotEmpty()) {
                    Text(text = emailError, color = Color.Red)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it.isEmpty()) "Campo obligatorio" else ""
            },
            label = { Text("Contraseña", fontSize = 16.sp) },
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = passwordError.isNotEmpty(),
            supportingText = {
                if (passwordError.isNotEmpty()) {
                    Text(text = passwordError, color = Color.Red)
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
                .clickable { navController.navigate(AppScreens.RecuperScreen.route) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de login normal
        Button(
            onClick = {
                emailError = ""
                passwordError = ""

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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
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

        // Botón de Google Sign-In mejorado
        OutlinedButton(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            AsyncImage(
                model = "https://developers.google.com/identity/images/g-logo.png",
                contentDescription = "Google Logo",
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continuar con Google",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF052659)),
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