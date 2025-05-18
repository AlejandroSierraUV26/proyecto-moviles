package com.example.proyecto.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.ui.auth.LoginScreen
import com.example.proyecto.ui.auth.RegisterScreen
import com.example.proyecto.ui.splash.SplashScreen
import com.example.proyecto.ui.home.HomeScreen
import com.example.proyecto.ui.courses.CoursesScreen
import com.example.proyecto.ui.courses.CoursesViewModel
import com.example.proyecto.ui.profile.ProfileScreen
import com.example.proyecto.ui.settings.SettingsScreen
import com.example.proyecto.ui.settings.EditProfileScreen
import com.example.proyecto.ui.settings.DeleteCourseScreen
import com.example.proyecto.ui.courses.CreateCoursesScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val viewModel: CoursesViewModel = viewModel()

    Scaffold(
        topBar = {
            if (currentRoute in listOf(
                    AppScreens.HomeScreen.route,
                    AppScreens.CoursesScreen.route,
                    AppScreens.ProfileScreen.route,
                    AppScreens.SettingsScreen.route
                )
            ) {
                TopBar(navController = navController)
            }
        },
        bottomBar = {
            if (currentRoute in listOf(
                    AppScreens.HomeScreen.route,
                    AppScreens.CoursesScreen.route,
                    AppScreens.ProfileScreen.route,
                    AppScreens.SettingsScreen.route
                )
            ) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreens.SplashScreen.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppScreens.SplashScreen.route) {
                SplashScreen(navController)
            }
            composable(AppScreens.LoginScreen.route) {
                LoginScreen(navController)
            }
            composable(AppScreens.RegisterScreen.route) {
                RegisterScreen(navController)
            }
            composable(AppScreens.HomeScreen.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(AppScreens.CoursesScreen.route) {
                CoursesScreen(navController = navController, viewModel = viewModel)
            }
            composable(AppScreens.CreateCourseScreen.route) {
                CreateCoursesScreen()
            }
            composable(AppScreens.ProfileScreen.route) {
                ProfileScreen()
            }
            composable(AppScreens.SettingsScreen.route) {
                SettingsScreen(navController)
            }
            composable(AppScreens.EditProfileScreen.route) {
                EditProfileScreen()
            }
            composable(AppScreens.DeleteCourseScreen.route) {
                DeleteCourseScreen()
            }
        }
    }
}