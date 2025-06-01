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
import com.example.proyecto.data.models.Exam

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val userCourses by viewModel.userCourses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val courseSections by viewModel.courseSections.collectAsState()
    val sectionExams by viewModel.sectionExams.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var expandedCourse by remember { mutableStateOf(false) }
    var expandedSections by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
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
                        .clickable { expandedCourse = !expandedCourse },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCourse?.title ?: "Selecciona un curso",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (expandedCourse) "Contraer" else "Expandir",
                        modifier = Modifier.rotate(if (expandedCourse) 180f else 0f)
                    )
                }

                if (expandedCourse && userCourses.isNotEmpty()) {
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
                                        expandedCourse = false
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

        // Botón para añadir experiencia
        Button(
            onClick = {
                viewModel.addExperience(100) // Añadimos 100 puntos de experiencia
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir experiencia (+100 XP)")
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
                    SectionCard(
                        section = section,
                        exams = sectionExams[section.id] ?: emptyList(),
                        isExpanded = expandedSections.contains(section.id),
                        onToggleExpand = {
                            expandedSections = if (expandedSections.contains(section.id)) {
                                expandedSections - section.id
                            } else {
                                (expandedSections + section.id).also {
                                    viewModel.loadExamsForSection(section.id)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    section: Section,
    exams: List<Exam>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                if (exams.isEmpty()) {
                    Text(
                        text = "No hay exámenes disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    exams.forEach { exam ->
                        ExamItem(exam = exam)
                        if (exam != exams.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamItem(exam: Exam) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar al examen */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = exam.title,
                style = MaterialTheme.typography.titleSmall
            )
            if (!exam.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exam.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Nivel de dificultad: ${getDifficultyText(exam.difficultyLevel)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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