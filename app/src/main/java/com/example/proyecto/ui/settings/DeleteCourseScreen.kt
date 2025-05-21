package com.example.proyecto.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.proyecto.R

@Composable
fun DeleteCourseScreen() {
    // Estado para controlar qué curso se está intentando eliminar
    var courseToDelete by remember { mutableStateOf<String?>(null) }
    
    // Lista de cursos como estado mutable
    var courses by remember { 
        mutableStateOf(listOf(
            "Curso 1", "Curso 2", "Curso 3", "Curso 4", "Curso 5", 
            "Curso 6", "Curso 7", "Curso 8", "Curso 9", "Curso 10", 
            "Curso 11", "Curso 12"
        )) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Eliminar Curso",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(courses) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = course,
                            fontSize = 18.sp
                        )
                        
                        IconButton(
                            onClick = { courseToDelete = course }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Eliminar curso",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
        
        // AlertDialog para confirmar la eliminación
        if (courseToDelete != null) {
            AlertDialog(
                onDismissRequest = { courseToDelete = null },
                title = {
                    Text(
                        text = "Eliminando Curso",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("¿Estás seguro que deseas eliminar este curso?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // TODO: Implementar lógica para eliminar curso
                            // Eliminar el curso de la lista
                            courses = courses.filter { it != courseToDelete }
                            courseToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { courseToDelete = null }
                    ) {
                        Text("No")
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            )
        }
    }
} 