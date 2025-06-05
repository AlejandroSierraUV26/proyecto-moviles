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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.R
import com.example.proyecto.data.models.Course

@Composable
fun DeleteCourseScreen(
    viewModel: DeleteCourseViewModel = viewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Estado para controlar qué curso se está intentando eliminar
    var courseToDelete by remember { mutableStateOf<Course?>(null) }

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
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (error != null) {
            Text(
                text = error ?: "",
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
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
                                text = course.title,
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
                    Text("¿Estás seguro que deseas eliminar el curso ${courseToDelete?.title}?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            courseToDelete?.let { course ->
                                viewModel.deleteCourse(course.id)
                            }
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