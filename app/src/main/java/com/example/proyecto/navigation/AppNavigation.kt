package com.example.proyecto.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto.navigation.AppScreens.ResultadosModuloScreen
import com.example.proyecto.ui.auth.LoginScreen
import com.example.proyecto.ui.auth.RecuperScreen
import com.example.proyecto.ui.auth.RegisterScreen
import com.example.proyecto.ui.splash.SplashScreen
import com.example.proyecto.ui.home.HomeScreen
import com.example.proyecto.ui.home.HomeViewModel
import com.example.proyecto.ui.courses.CoursesScreen
import com.example.proyecto.ui.courses.CoursesViewModel
import com.example.proyecto.ui.profile.ProfileScreen
import com.example.proyecto.ui.settings.SettingsScreen
import com.example.proyecto.ui.settings.EditProfileScreen
import com.example.proyecto.ui.settings.DeleteCourseScreen
import com.example.proyecto.ui.courses.CreateCoursesScreen
import com.example.proyecto.ui.modules.CargaScreen
import com.example.proyecto.ui.modules.CourseEntryScreen
import com.example.proyecto.ui.modules.NivelUsuarioScreen
import com.example.proyecto.ui.modules.QuestionScreen
import com.example.proyecto.ui.modules.QuizViewModel
import com.example.proyecto.ui.modules.ResultadosModuloScreen
import com.example.proyecto.ui.modules.ResultadosViewModel
import com.example.proyecto.ui.modules.SeleCourseScreen
import com.example.proyecto.ui.modules.SetPreguntasScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val coursesViewModel: CoursesViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

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
                HomeScreen(navController, viewModel = homeViewModel)
            }
            composable(AppScreens.CoursesScreen.route) {
                CoursesScreen(navController = navController, viewModel = coursesViewModel)
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
                EditProfileScreen(navController = navController)
            }
            composable(AppScreens.DeleteCourseScreen.route) {
                DeleteCourseScreen()
            }
            composable(AppScreens.NivelUsuarioScreen.route) {
                NivelUsuarioScreen(navController)
            }
            // En tu archivo de navegación (AppNavigation.kt o similar)

            composable(
                route = "${AppScreens.SetPreguntasSreen.route}/{sectionId}/{difficultyLevel}",
                arguments = listOf(
                    navArgument("sectionId") { type = NavType.IntType },
                    navArgument("difficultyLevel") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getInt("sectionId") ?: 0
                val difficultyLevel = backStackEntry.arguments?.getInt("difficultyLevel") ?: 1

                SetPreguntasScreen(
                    sectionId = sectionId,
                    difficultyLevel = difficultyLevel,
                    onBack = { navController.popBackStack() }, // Esta es la forma correcta
                    onQuestionCreated = { navController.popBackStack() }
                )
            }
            composable(AppScreens.SeleCourseScreen.route) {
                SeleCourseScreen(navController)
            }
            composable(AppScreens.RecuperScreen.route) {
                RecuperScreen(navController)
            }
            composable(AppScreens.CourseEntryScreen.route) {
                CourseEntryScreen(navController)
            }
            composable(
                route = "setPreguntas/{sectionId}/{difficultyLevel}",
                arguments = listOf(
                    navArgument("sectionId") { type = NavType.IntType },
                    navArgument("difficultyLevel") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getInt("sectionId") ?: 0
                val difficultyLevel = backStackEntry.arguments?.getInt("difficultyLevel") ?: 1

                SetPreguntasScreen(
                    sectionId = sectionId,
                    difficultyLevel = difficultyLevel,
                    onBack = { navController.popBackStack() },
                    onQuestionCreated = { /* Puedes mostrar un Snackbar o algo */ }
                )
            }
            composable(
                route = "questions/{examId}",
                arguments = listOf(navArgument("examId") { type = NavType.IntType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getInt("examId") ?: 0
                val currentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("questions/$examId")
                }
                val quizViewModel: QuizViewModel = viewModel(currentEntry)

                QuestionScreen(
                    examId = examId,
                    navController = navController,
                    quizViewModel = quizViewModel
                )
            }

            composable(
                route = "resultados_modulo/{moduloId}",
                arguments = listOf(navArgument("moduloId") { type = NavType.IntType })
            ) { backStackEntry ->
                val examId = backStackEntry.arguments?.getInt("moduloId") ?: 0

                // 🔧 Aquí usamos la ruta concreta con el examId
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("questions/$examId")
                }
                val quizViewModel: QuizViewModel = viewModel(parentEntry)

                // ViewModel de resultados
                val resultadosViewModel: ResultadosViewModel = viewModel()

                // Transferir respuestas del quiz al viewModel de resultados
                LaunchedEffect(key1 = examId) {
                    val answers = quizViewModel.userAnswers.value
                    resultadosViewModel.setUserAnswers(answers)
                    resultadosViewModel.submitExam(examId, answers)
                }

                ResultadosModuloScreen(
                    examId = examId,
                    navController = navController,
                    viewModel = resultadosViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}