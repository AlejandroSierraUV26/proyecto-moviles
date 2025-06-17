package com.example.proyecto.ui.modules

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.proyecto.navigation.AppScreens
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

@Composable
fun ReviewAnswersScreen(
    navController: NavController,
    answersJson: String
) {
    val answers = remember {
        try {
            Gson().fromJson(answersJson, object : TypeToken<Map<Int, String>>() {}.type)
        } catch (e: Exception) {
            emptyMap<Int, String>()
        }
    }

    // Mostrar las respuestas del usuario
    if (answers.isNotEmpty()) {
        // Aquí puedes implementar la UI para mostrar las respuestas
        // Por ejemplo, un LazyColumn que muestre cada pregunta y su respuesta
        // Puedes usar un Composable como Text para mostrar cada respuesta
        answers.forEach { (questionId, answer) ->
            // Mostrar cada pregunta y su respuesta
            Text(text = "Pregunta $questionId: $answer")
        }
    } else {
        // Mostrar un mensaje si no hay respuestas
        Text(text = "No se encontraron respuestas.")
    }
    // Aquí puedes agregar botones para navegar a otras pantallas o realizar acciones
    // Por ejemplo, un botón para volver al inicio

    Button(onClick = { navController.navigate(AppScreens.HomeScreen.route) }) {
    }
        Text(text = "Volver al inicio")
    }
