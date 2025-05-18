package com.backtor.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.backtor.security.JwtService
import com.auth0.jwt.JWTVerifier

fun Application.configureSecurity() {
    val jwtVerifier = JWT
        .require(Algorithm.HMAC256("rE7k4FQFyOdulJHySsuqJ1hzYPVydsT5zKGtmpoK5ywy8dNnK6RqtcQoxS4OZ1YK0pQAwykKEuUICvDgC20r+g=="))
        .withIssuer("ktor.io")
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "access"
            verifier(jwtVerifier)
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }

}
