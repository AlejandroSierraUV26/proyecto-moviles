package com.example.proyecto.navigation

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
        // Nueva definición que coincide con el backend
        fun createRoute(
            levelTested: Int,
            passed: Boolean,
            score: Double,
            startingSection: String,
            message: String
        ): String {
            return "diagnostic_results/" +
                    "levelTested=$levelTested&" +
                    "passed=$passed&" +
                    "score=$score&" +
                    "startingSection=${URLEncoder.encode(startingSection, "UTF-8")}&" +
                    "message=${URLEncoder.encode(message, "UTF-8")}"
        }
    }
}