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
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.backtor.routes.userRoutes
import com.backtor.routes.examRoutes
import com.backtor.database.DatabaseFactory
import com.backtor.security.JwtService

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "backtor-api"
            verifier(JwtService.getVerifier())
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                if (!email.isNullOrEmpty()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token inválido o expirado")
            }
        }
    }

    routing {
        userRoutes()
        examRoutes()
    }
}


