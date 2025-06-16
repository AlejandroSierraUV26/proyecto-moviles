package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.navigation.AppNavigation
import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.ui.home.HomeViewModel
import com.example.proyecto.ui.home.HomeViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitClient.initialize(this)

        setContent {
            ProyectoTheme {
                // Proporciona el ViewModel con factory
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(application)
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(homeViewModel = homeViewModel)
                }
            }
        }
    }
}