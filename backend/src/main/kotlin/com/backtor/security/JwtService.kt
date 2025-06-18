package com.backtor.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*
import io.github.cdimascio.dotenv.dotenv
import com.auth0.jwt.interfaces.JWTVerifier


object JwtService {
    private const val issuer = "backtor-api"
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }
    private val secret: String = dotenv["JWT_SECRET"]
        ?: System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET no definida en .env o en entorno")

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(email: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject("UserAuthentication")
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000)) // 2 horas
            .sign(algorithm)
    }


    fun verifyToken(token: String): String? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
            val decodedJWT = verifier.verify(token)
            decodedJWT.getClaim("email").asString()
        } catch (e: Exception) {
            null // Token inválido o expirado
        }
    }
    fun getVerifier(): JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()
}
