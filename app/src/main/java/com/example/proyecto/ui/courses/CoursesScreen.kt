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
import com.example.proyecto.data.models.Course
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

@Composable
fun CoursesScreen(
    navController: NavController,
    viewModel: CoursesViewModel = viewModel()
) {
    val context = LocalContext.current
    val availableCourses by viewModel.availableCourses.collectAsState()
    
    // Cargar los cursos disponibles cuando se inicia el CoursesScreen
    LaunchedEffect(Unit) {
        viewModel.loadAvailableCourses()
    }
    
    DoubleBackToExitHandler {
        android.os.Process.killProcess(android.os.Process.myPid())
    }
    
    var searchText by remember { mutableStateOf("") }
    var expandedCourseId by remember { mutableStateOf<Int?>(null) }
    
    val filteredCourses = availableCourses.filter { 
        it.description.contains(searchText, ignoreCase = true) ||
        it.title.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
    var showSuccessDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.title,
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
                            viewModel.addCourseToUser(course.id)
                            showSuccessDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    text = "¡Éxito!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
            },
            text = {
                Text("El curso ha sido agregado correctamente")
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green
                    )
                ) {
                    Text("Aceptar")
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}