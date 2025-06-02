package com.example.proyecto.ui.modules

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPreguntasScreen(
    sectionId: Int,
    difficultyLevel: Int,
    onBack: () -> Unit, // Nuevo parámetro para manejar el retroceso
    onQuestionCreated: () -> Unit, // Nuevo parámetro para manejar creación exitosa
    viewModel: SetPreguntasViewModel = viewModel()
) {
    var questionText by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "", "", "")) }
    var correctAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }

    val creationState by viewModel.creationState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear nueva pregunta",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Texto de la pregunta") },
                modifier = Modifier.fillMaxWidth()
            )

            options.forEachIndexed { index, option ->
                OutlinedTextField(
                    value = option,
                    onValueChange = {
                        options = options.toMutableList().apply { set(index, it) }
                        if (correctAnswer == option) correctAnswer = it
                    },
                    label = { Text("Opción ${index + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = correctAnswer,
                onValueChange = { correctAnswer = it },
                label = { Text("Respuesta correcta (debe coincidir con una opción)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Retroalimentación") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.createQuestion(
                        sectionId = sectionId,
                        difficultyLevel = difficultyLevel,
                        questionText = questionText,
                        options = options.filter { it.isNotBlank() },
                        correctAnswer = correctAnswer,
                        feedback = feedback
                    )
                },
                modifier = Modifier.align(Alignment.End),
                enabled = questionText.isNotBlank() &&
                        options.count { it.isNotBlank() } >= 2 &&
                        correctAnswer.isNotBlank() &&
                        feedback.isNotBlank()
            ) {
                Text("Crear Pregunta")
            }

            when (val state = creationState) {
                is SetPreguntasViewModel.CreationState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is SetPreguntasViewModel.CreationState.Success -> {
                    LaunchedEffect(state) {
                        onQuestionCreated() // Navegar atrás o a otra pantalla
                    }
                }
                is SetPreguntasViewModel.CreationState.Error -> {
                    AlertDialog(
                        onDismissRequest = { /* No action */ },
                        title = { Text("Error") },
                        text = { Text(state.message) },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.resetCreationState()
                            }) {
                                Text("OK")
                            }
                        }
                    )
                }
                else -> {}
            }
        }
    }
}