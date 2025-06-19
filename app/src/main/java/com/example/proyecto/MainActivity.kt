package com.example.proyecto

// 🔹 Kotlin/Android estándar
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

// 🔹 AndroidX y Jetpack
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder


// 🔹 Proyecto local
import com.example.proyecto.data.api.RetrofitClient
import com.example.proyecto.navigation.AppNavigation
import com.example.proyecto.ui.home.HomeViewModel
import com.example.proyecto.ui.home.HomeViewModelFactory
import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.utils.NotificationReceiver
import com.example.proyecto.utils.NotificationWorker
import com.example.proyecto.utils.NotificationHelper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color




// 🔹 Utilidades
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔐 Pedir permiso para mostrar notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        // 🔁 Notificación cada minuto (BroadcastReceiver + AlarmManager)
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval = 60_000L // 1 minuto

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            interval,
            pendingIntent
        )

        // 🌐 Inicializa Retrofit
        RetrofitClient.initialize(this)

        // 📆 Notificación cada 15 minutos (WorkManager)
        val workTag = "notificacion_periodica"
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            workTag,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()
        )

        // 🔔 Forzar una notificación inmediata al iniciar la app
        val testIntent = Intent(this, NotificationReceiver::class.java)
        sendBroadcast(testIntent)

        // 🎨 Composable UI
        setContent {
            ProyectoTheme {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(application)
                )

                setContent {
                    ProyectoTheme {
                        val homeViewModel: HomeViewModel = viewModel(
                            factory = HomeViewModelFactory(application)
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            // Navegación principal
                            AppNavigation(homeViewModel = homeViewModel)

                            // Botón de notificación arriba y centrado
                            Button(
                                onClick = {
                                    NotificationHelper.showNotification(
                                        context = this@MainActivity,
                                        title = "📚 Tu estudio diario",
                                        message = "¿Ya revisaste algo de matemáticas hoy?"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 24.dp)
                                    .height(36.dp)
                            ) {
                                Text("Notificar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}



                    // O si quieres mantener la navegación y el botón:
                    // Column {
                    //     AppNavigation(homeViewModel = homeViewModel)
                    //     Button(onClick = { /* misma lógica */ }) { Text("Probar notificación") }
                    // }