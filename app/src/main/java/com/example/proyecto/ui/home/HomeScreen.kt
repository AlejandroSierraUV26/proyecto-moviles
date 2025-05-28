package com.example.proyecto.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.utils.DoubleBackToExitHandler
import com.example.proyecto.data.models.Course
import com.example.proyecto.data.models.Section

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val userCourses by viewModel.userCourses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val courseSections by viewModel.courseSections.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    
    // Cargar los cursos cuando se inicia el HomeScreen
    LaunchedEffect(Unit) {
        viewModel.loadUserCourses()
    }
    
    DoubleBackToExitHandler {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Selector de cursos
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCourse?.title ?: "Selecciona un curso",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Contraer" else "Expandir",
                        modifier = Modifier.rotate(if (expanded) 180f else 0f)
                    )
                }

                if (expanded && userCourses.isNotEmpty()) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        userCourses.forEach { course ->
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectCourse(course)
                                        expanded = false
                                    }
                                    .padding(8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar loading si está cargando
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Mostrar error si existe y no está cargando
        if (error != null && !isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mostrar secciones si hay un curso seleccionado y no está cargando
        if (selectedCourse != null && !isLoading) {
            LazyColumn {
                items(courseSections) { section ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = section.title,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(section: Section) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Nivel de dificultad: ${getDifficultyText(section.difficultyLevel)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun getDifficultyText(level: Int): String {
    return when (level) {
        1 -> "Fácil"
        2 -> "Intermedio"
        3 -> "Difícil"
        else -> "Nivel $level"
    }
}

@Composable
private fun CourseItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium.copy(
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}