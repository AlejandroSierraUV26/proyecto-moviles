package com.backtor


import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import com.backtor.models.UserRegisterRequest
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import com.backtor.routes.userRoutes
import com.backtor.database.DatabaseFactory

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            userRoutes() // 👈 Asegúrate de tener esto
        }
    }.start(wait = true)
}


