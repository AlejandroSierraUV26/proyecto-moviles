package com.example.proyecto.navigation

import androidx.navigation.NavBackStackEntry
import com.example.proyecto.data.models.DiagnosticFeedback
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder


sealed class AppScreens(open val route: String){
    object SplashScreen: AppScreens("splash_screen")
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object HomeScreen: AppScreens("home_screen")
    object CoursesScreen: AppScreens("courses_screen")
    object CreateCourseScreen: AppScreens("create_course_screen")
    object ProfileScreen: AppScreens("profile_screen")
    object SettingsScreen: AppScreens("settings_screen")
    object EditProfileScreen: AppScreens("edit_profile")
    object DeleteCourseScreen: AppScreens("delete_course_screen")
    object CargaScreen : AppScreens("carga_screen")
    object CourseEntryScreen : AppScreens("Curso Inicio")
    object SetPreguntasSreen : AppScreens("set_preguntas")
    object ResultadosModuloScreen : AppScreens("resultados_modulo")
    object RecuperScreen : AppScreens("recuperacion-contraseña")
    object SeleCourseScreen: AppScreens("seleccion-curso")
    object NivelUsuarioScreen : AppScreens("nivel_usuario")

    object DiagnosticScreen : AppScreens("diagnostic/{courseId}/{level}") {
        fun createRoute(courseId: Int, level: Int) = "diagnostic/$courseId/$level"
    }

    object DiagnosticResults : AppScreens("diagnostic_results") {
        // Ruta base sin argumentos
        const val ROUTE = "diagnostic_results"

        // Ruta con parámetros definidos (para usar en NavHost)
        const val routeWithArgs = "$ROUTE?feedbackJson={feedbackJson}"

        // Función para crear la ruta con argumentos
        fun createRoute(feedback: DiagnosticFeedback): String {
            val json = Gson().toJson(feedback)
            val encodedJson = URLEncoder.encode(json, "UTF-8")
            return "$ROUTE?feedbackJson=$encodedJson"
        }

        // Función para parsear los argumentos (opcional, útil si necesitas parsear en varios lugares)
        fun parseFeedback(backStackEntry: NavBackStackEntry): DiagnosticFeedback? {
            val json = backStackEntry.arguments?.getString("feedbackJson") ?: return null
            val decodedJson = URLDecoder.decode(json, "UTF-8")
            return try {
                Gson().fromJson(decodedJson, DiagnosticFeedback::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}