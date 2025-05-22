package com.example.proyecto.navigation

sealed class AppScreens(val route: String){
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
    object NivelUsuarioScreen: AppScreens("nivel_screen")
}