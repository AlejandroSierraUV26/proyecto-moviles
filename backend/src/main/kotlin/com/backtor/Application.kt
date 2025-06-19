package com.backtor

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import com.backtor.routes.userRoutes
import com.backtor.routes.examRoutes
import com.backtor.database.DatabaseFactory
import com.backtor.security.JwtService

fun main(args: Array<String>) {
    dotenv { ignoreIfMissing = true }
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    DatabaseFactory.init()
    
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            classDiscriminator = "type"
        })
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


