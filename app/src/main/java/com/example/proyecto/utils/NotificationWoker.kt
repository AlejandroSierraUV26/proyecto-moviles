package com.example.proyecto.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.proyecto.R

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val horaActual = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        if (horaActual in 8..22) {
            val frases = listOf(
                "¡Hora de repasar algo de matemáticas! ✏️",
                "¿Ya resolviste un problema de física hoy? ⚛️",
                "Revisar un módulo diario te lleva al éxito 📚",
                "Tu progreso en ciencias te está esperando 🔬",
                "No olvides completar tu lección de hoy 🎯",
            )
            val mensaje = frases.random()
            showNotification("¡Tu estudio diario!", mensaje)
        } else {
            // No mostrar notificación fuera del horario razonable
            println("Hora actual $horaActual: fuera del horario de notificación.")
        }

        return Result.success()
    }


    private fun showNotification(title: String, message: String) {
        val channelId = "default_channel_id"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Canal de Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_study_reminder)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
