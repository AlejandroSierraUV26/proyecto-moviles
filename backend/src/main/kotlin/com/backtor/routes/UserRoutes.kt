package com.backtor.routes

import com.backtor.models.UserRegisterRequest
import com.backtor.models.UserLoginRequest
import com.backtor.models.ApiResponse
import com.backtor.services.UserService



import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

import io.ktor.server.application.*
import kotlinx.coroutines.launch




import java.util.UUID

import org.mindrot.jbcrypt.BCrypt

// Crea instancia del servicio
val userService = UserService()

fun Route.userRoutes() {
    route("/api") {
        post("/register") {
            val userRequest = call.receive<UserRegisterRequest>()

            if (userService.findByEmail(userRequest.email) != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse(success = false, message = "El usuario ya existe")
                )
                return@post
            }

            try {
                userService.saveUser(userRequest)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(success = true, message = "Usuario registrado correctamente")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(success = false, message = "Error al registrar usuario")
                )
            }
        }
        post("/login") {
            val loginRequest = call.receive<UserLoginRequest>()

            val user = userService.findByEmail(loginRequest.email)
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(success = false, message = "Credenciales inválidas")
                )
                return@post
            }

            val userPasswordHash = userService.getPasswordHashByEmail(loginRequest.email)
            if (userPasswordHash == null || !BCrypt.checkpw(loginRequest.password, userPasswordHash)) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(success = false, message = "Credenciales inválidas")
                )
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(success = true, message = "Login exitoso")
            )
        }

        get("/profile") {
            val email = call.request.queryParameters["email"]
            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                return@get
            }

            val user = userService.getUserProfile(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
            } else {
                call.respond(user)
            }
        }

        put("/update-username") {
            val email = call.request.queryParameters["email"]
            val newUsername = call.request.queryParameters["username"]
            
            if (email == null || newUsername == null) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ApiResponse(false, "Email y nuevo nombre de usuario son requeridos")
                )
                return@put
            }
            
            val success = userService.updateUserProfile(email, newUsername)
            if (success) {
                call.respond(
                    HttpStatusCode.OK, 
                    ApiResponse(true, "Nombre de usuario actualizado correctamente")
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound, 
                    ApiResponse(false, "Usuario no encontrado")
                )
            }
        }

        put("/update-password") {
            val request = try {
                call.receive<Map<String, String>>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ApiResponse(false, "Datos inválidos")
                )
                return@put
            }
            
            val email = request["email"]
            val newPassword = request["newPassword"]
            
            if (email == null || newPassword == null) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ApiResponse(false, "Email y nueva contraseña son requeridos")
                )
                return@put
            }
            
            val success = userService.updateUserPassword(email, newPassword)
            if (success) {
                call.respond(
                    HttpStatusCode.OK, 
                    ApiResponse(true, "Contraseña actualizada correctamente")
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound, 
                    ApiResponse(false, "Usuario no encontrado")
                )
            }
        }

        delete("/delete") {
            val email = call.request.queryParameters["email"]
            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                return@delete
            }

            val user = userService.getUserProfile(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
            } else {
                userService.deleteUser(email)
                call.respond(HttpStatusCode.OK, ApiResponse(true, "Usuario eliminado"))
            }
        }

        put("/streak") {
            val email = call.request.queryParameters["email"]
            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                return@put
            }
        
            val user = userService.getUserProfile(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
            } else {
                val success = userService.updateStreak(email)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Streak actualizado"))
                } else {
                    call.respond(HttpStatusCode.Forbidden, ApiResponse(false, "Ya has actualizado tu streak hoy"))
                }
            }
        }
        put("/reset_streak"){
            val email = call.request.queryParameters["email"]
            if (email == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                return@put
            }

            val user = userService.getUserProfile(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
            } else {
                userService.resetStreak(email)
                call.respond(HttpStatusCode.OK, ApiResponse(true, "Streak reseteado"))
            }
        }
        post("/forgot-password") {
            val email = call.receive<Map<String, String>>()["email"]

            if (email.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                return@post
            }

            val user = userService.findByEmail(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
                return@post
            }

            val token = (1000..9999).random()
            userService.savePasswordResetToken(email, token)

            // Lanza el envío de correo en segundo plano (no bloquea la conexión)
            call.application.launch {
                userService.sendEmail(email, "Recupera tu contraseña", "Tu token es: $token")
            }

            call.respond(HttpStatusCode.OK, ApiResponse(true, "Se ha enviado un correo con las instrucciones"))
        }

        post("/reset-password") {
            val request = call.receive<Map<String, String>>()
            val email = request["email"]
            val token = request["token"]
            val newPassword = request["newPassword"]

            if (email.isNullOrEmpty() || token.isNullOrEmpty() || newPassword.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Todos los campos son requeridos"))
                return@post
            }

            val isValidToken = userService.validatePasswordResetToken(email, token.toInt())
            if (!isValidToken) {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "Token inválido o expirado"))
                return@post
            }

            val success = userService.updateUserPassword(email, newPassword)
            if (success) {
                call.respond(HttpStatusCode.OK, ApiResponse(true, "Contraseña restablecida correctamente"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al restablecer la contraseña"))
            }
        }
    }
}
