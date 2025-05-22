package com.example.proyecto

import android.app.Application
import com.example.proyecto.data.api.RetrofitClient

class ProyectoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
    }
} 