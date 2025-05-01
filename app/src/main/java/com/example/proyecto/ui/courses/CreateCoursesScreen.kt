package com.example.proyecto.ui.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateCoursesScreen() {
    var courseDescription by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Creando curso",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Subtítulo
        Text(
            text = "Explicación",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Descripción
        Text(
            text = "Para crear un curso, debes proporcionar una descripción detallada del tema que deseas aprender. " +
                   "Sé específico y claro en tu explicación, incluyendo los conceptos principales y los objetivos de aprendizaje. " +
                   "Cuanto más detallada sea tu descripción, mejor podremos generar un curso personalizado para ti.",
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Campo de texto flexible
        OutlinedTextField(
            value = courseDescription,
            onValueChange = { courseDescription = it },
            label = { 
                Text(
                    "Descripción del curso",
                    fontSize = 18.sp
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(fontSize = 16.sp),
            minLines = 5
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botón Generar
        Button(
            onClick = { /* TODO: Implementar lógica para generar curso */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Generar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 