package com.example.proyecto.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyecto.utils.DoubleBackToExitHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.ui.courses.Course
import com.example.proyecto.ui.courses.CoursesViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(viewModel: CoursesViewModel = viewModel()) {
    val courses by remember { derivedStateOf { viewModel.courses } }
    val selectedCourse by remember { derivedStateOf { viewModel.selectedCourse } }
    var expanded by remember { mutableStateOf(false) }
    
    DoubleBackToExitHandler {
        // Cierra la aplicación
        android.os.Process.killProcess(android.os.Process.myPid())
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedCourse ?: "Selecciona un curso",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    if (courses.isNotEmpty()) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = if (expanded) "Contraer" else "Expandir",
                                modifier = Modifier.rotate(if (expanded) 180f else 0f)
                            )
                        }
                    }
                }

                if (expanded && courses.isNotEmpty()) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        courses.forEach { courseName ->
                            Text(
                                text = courseName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectCourse(courseName)
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

        selectedCourse?.let { course ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Contenido $course",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 18.sp
                    )
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
} 