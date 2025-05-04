package com.example.proyecto.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.navigation.AppScreens
import com.example.proyecto.utils.DoubleBackToExitHandler

data class Course(
    val id: String,
    val name: String,
    val description: String
)

@Composable
fun CoursesScreen(navController: NavController,
                  viewModel: CoursesViewModel = viewModel()) {
    val context = LocalContext.current
    
    DoubleBackToExitHandler {
        // Cierra la aplicación
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    // Estado para el texto de búsqueda
    var searchText by remember { mutableStateOf("") }
    
    // Estado para controlar qué curso está expandido
    var expandedCourseId by remember { mutableStateOf<String?>(null) }
    
    // Lista de cursos de ejemplo
    val courses = remember {
        listOf(
            Course("1", "Curso 1", "Descripción detallada del curso 1"),
            Course("2", "Curso 2", "Descripción detallada del curso 2"),
            Course("3", "Curso 3", "Descripción detallada del curso 3"),
            Course("4", "Curso 4", "Descripción detallada del curso 4"),
            Course("5", "Curso 5", "Descripción detallada del curso 5")
        )
    }
    
    // Filtrar cursos basado en el texto de búsqueda
    val filteredCourses = courses.filter { 
        it.description.contains(searchText, ignoreCase = true) 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Campo de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { 
                Text(
                    text = "Buscar cursos",
                    fontSize = 16.sp
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = TextStyle(fontSize = 18.sp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de cursos
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCourses) { course ->
                CourseCard(
                    course = course,
                    isExpanded = expandedCourseId == course.id,
                    onExpandClick = {
                        expandedCourseId = if (expandedCourseId == course.id) null else course.id
                    },
                    viewModel = viewModel
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(AppScreens.CreateCourseScreen.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Generar curso",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    viewModel: CoursesViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Encabezado de la tarjeta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                    )
                }
            }
            
            // Contenido expandido
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = course.description,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            viewModel.addCourse(course.name)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}