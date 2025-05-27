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
        // Card superior para selección de curso
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedCourse?.title ?: "Selecciona un curso",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    if (userCourses.isNotEmpty()) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (expanded) "Contraer" else "Expandir",
                                modifier = Modifier.rotate(if (expanded) 180f else 0f)
                            )
                        }
                    }
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

        // Mostrar error si existe
        error?.let { errorMessage ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lista de secciones del curso seleccionado
        if (selectedCourse != null) {
            if (courseSections.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay secciones disponibles para este curso",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(courseSections) { section ->
                        SectionCard(section = section)
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