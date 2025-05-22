package com.example.proyecto.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.utils.SecurePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val securePreferences = SecurePreferences(context)
    var showLogoutDialog by remember { mutableStateOf(false) }

    val title = when (currentRoute) {
        AppScreens.HomeScreen.route -> "Inicio"
        AppScreens.CoursesScreen.route -> "Cursos"
        AppScreens.ProfileScreen.route -> "Perfil"
        AppScreens.SettingsScreen.route -> "Ajustes"
        else -> ""
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { 
                Text(
                    text = "Cerrando Sesión",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        securePreferences.clearSession()
                        navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Sí", color = Color(0xFF052659))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF052659)
                    ),
                    border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF052659), Color(0xFF052659))
                        )
                    )
                ) {
                    Text("No")
                }
            }
        )
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        actions = {
            IconButton(onClick = { showLogoutDialog = true }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF052659),
            titleContentColor = Color.White
        )
    )
} 