package com.example.proyecto.navigation

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.proyecto.ui.auth.PasswordRecoveryViewModel
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
import com.example.proyecto.ui.home.HomeViewModelFactory
import com.example.proyecto.ui.modules.CargaScreen
import com.example.proyecto.ui.modules.CourseEntryScreen
import com.example.proyecto.ui.modules.DiagnosticResultsScreen
import com.example.proyecto.ui.modules.DiagnosticScreen
import com.example.proyecto.ui.modules.DiagnosticViewModel
import com.example.proyecto.ui.modules.DiagnosticViewModelFactory
import com.example.proyecto.ui.modules.NivelUsuarioScreen
import com.example.proyecto.ui.modules.QuestionScreen
import com.example.proyecto.ui.modules.QuizViewModel
import com.example.proyecto.ui.modules.ResultadosModuloScreen
import com.example.proyecto.ui.modules.ResultadosViewModel
import com.example.proyecto.ui.modules.SeleCourseScreen
import com.example.proyecto.ui.modules.SetPreguntasScreen
import kotlinx.coroutines.delay
import java.net.URLDecoder
import kotlin.math.max

@Composable
fun AppNavigation(homeViewModel: HomeViewModel) {
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
            composable(AppScreens.RecuperScreen.route) {
                val viewModel: PasswordRecoveryViewModel = viewModel()

                LaunchedEffect(viewModel.successMessage) {
                    if (viewModel.successMessage == "Contraseña restablecida correctamente") {
                        delay(2000)
                        navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.RecuperScreen.route) { inclusive = true }
                        }
                    }
                }

                RecuperScreen(navController, viewModel)
            }
            composable(AppScreens.HomeScreen.route) {
                HomeScreen(navController)
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
            composable(AppScreens.CourseEntryScreen.route) {
                CourseEntryScreen(navController)
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

            composable(AppScreens.RecuperScreen.route) {
                RecuperScreen(navController)
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
            composable(AppScreens.SeleCourseScreen.route) {
                SeleCourseScreen(
                    navController = navController,
                    coursesViewModel = coursesViewModel,
                    homeViewModel = homeViewModel
                )
            }

            composable(
                route = "diagnostic/{courseId}/{level}",  // Asegúrate que coincida exactamente
                arguments = listOf(
                    navArgument("courseId") { type = NavType.IntType },
                    navArgument("level") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                val level = backStackEntry.arguments?.getString("level") ?: "basic"

                // Usa el ViewModel con factory
                val factory = DiagnosticViewModelFactory(LocalContext.current.applicationContext as Application)
                val viewModel: DiagnosticViewModel = viewModel(factory = factory)

                DiagnosticScreen(
                    courseId = courseId,
                    level = level,
                    navController = navController,
                )
            }

            composable(AppScreens.NivelUsuarioScreen.route) {
                NivelUsuarioScreen(
                    navController = navController,
                    coursesViewModel = coursesViewModel,
                    homeViewModel = homeViewModel
                )
            }
            composable(
                route = AppScreens.DiagnosticScreen.route,
                arguments = listOf(
                    navArgument("courseId") { type = NavType.IntType },
                    navArgument("level") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                val level = backStackEntry.arguments?.getString("level") ?: "basic"

                DiagnosticScreen(
                    courseId = courseId,
                    level = level,
                    navController = navController
                )
            }
            // En tu NavGraph principal
            composable(
                route = AppScreens.DiagnosticResults.route + "/{courseId}/{level}/{startingSection}/{message}/{correctAnswers}/{totalQuestions}",
                arguments = listOf(
                    navArgument("courseId") { type = NavType.IntType },
                    navArgument("level") { type = NavType.StringType },
                    navArgument("startingSection") { type = NavType.StringType },
                    navArgument("message") { type = NavType.StringType },
                    navArgument("correctAnswers") { type = NavType.IntType },
                    navArgument("totalQuestions") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                val level = backStackEntry.arguments?.getString("level") ?: "basic"
                val startingSection = backStackEntry.arguments?.getString("startingSection") ?: "1"
                val message = URLDecoder.decode(
                    backStackEntry.arguments?.getString("message") ?: "",
                    "UTF-8"
                )
                val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0
                val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 1

                DiagnosticResultsScreen(
                    courseId = courseId,
                    level = level,
                    startingSection = startingSection,
                    message = message,
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    navController = navController
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