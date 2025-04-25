package com.example.proyecto.navigation

sealed class AppScreens(val route: String){
    object SplashScreen: AppScreens("splash_screen")
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object HomeScreen: AppScreens("home_screen")
    object CoursesScreen: AppScreens("courses_screen")
    object ProfileScreen: AppScreens("profile_screen")
    object SettingsScreen: AppScreens("settings_screen")
    object EditProfileScreen: AppScreens("edit_profile_screen")
    object DeleteCourseScreen: AppScreens("delete_course_screen")
}