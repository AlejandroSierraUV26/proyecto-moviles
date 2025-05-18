package com.example.proyecto.utils

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DoubleBackToExitHandler(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var backPressedState by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<Toast?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(backPressedState) {
        if (backPressedState) {
            delay(2000)
            backPressedState = false
            toast?.cancel()
            toast = null
        }
    }

    BackHandler {
        if (backPressedState) {
            toast?.cancel()
            toast = null
            onBackPressed()
        } else {
            backPressedState = true
            toast = Toast.makeText(context, "Presiona otra vez para salir", Toast.LENGTH_SHORT).apply {
                show()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(bottom = 16.dp)
        ) { data ->
            Snackbar(
                containerColor = Color(0xFF052659),
                contentColor = Color.White
            ) {
                androidx.compose.material3.Text(data.visuals.message)
            }
        }
    }
} 