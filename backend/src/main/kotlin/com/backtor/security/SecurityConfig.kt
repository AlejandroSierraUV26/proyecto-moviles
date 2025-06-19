package com.backtor.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.github.cdimascio.dotenv.dotenv
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.backtor.security.JwtService
import com.auth0.jwt.JWTVerifier

fun Application.configureSecurity() {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val jwtSecret = dotenv["JWT_SECRET"]
        ?: System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET no definida")
    val jwtVerifier = JWT
        .require(Algorithm.HMAC256(jwtSecret))
        .withIssuer("ktor.io")
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "access"
            verifier(JwtService.getVerifier())
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }

}
