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
                    ApiResponse(success = false, message = "El correo electrónico ya está registrado")
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
                if (e.message?.contains("users_username_key") == true) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ApiResponse(success = false, message = "El nombre de usuario ya está en uso")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse(success = false, message = "Error al registrar usuario")
                    )
                }
            }
        }
        post("/login") {
            val loginRequest = call.receive<UserLoginRequest>()

            val user = userService.findByIdentifier(loginRequest.identifier)
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(success = false, message = "Usuario no registrado")
                )
                return@post
            }

            val userPasswordHash = userService.getPasswordHashByIdentifier(loginRequest.identifier)
            if (userPasswordHash == null || !BCrypt.checkpw(loginRequest.password, userPasswordHash)) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(success = false, message = "Su usuario/correo o contraseña son erróneos")
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
        put("/update-profile") {
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
            val newUsername = request["username"]
            val password = request["password"]
            val confirmPassword = request["confirmPassword"]
            if (email == null || newUsername == null || password == null || confirmPassword == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "Email, nuevo nombre de usuario, contraseña y confirmación son requeridos")
                )
                return@put
            }
            val user = userService.getUserProfile(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
                return@put
            }
            val userPasswordHash = userService.getPasswordHashByEmail(email)
            if (userPasswordHash == null || !BCrypt.checkpw(password, userPasswordHash)) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(false, "Contraseña incorrecta")
                )
                return@put
            }
            if (password != confirmPassword) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "Las contraseñas no coinciden")
                )
                return@put
            }
            val success = userService.updateUserUsername(email, newUsername, password, confirmPassword)
            if (success) {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(true, "Nombre de usuario actualizado correctamente")
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al actualizar el nombre de usuario")
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
            val password = request["password"]
            val newPassword = request["newPassword"]
            
            if (email == null || newPassword == null || password == null) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ApiResponse(false, "Email, contraseña y nueva contraseña son requeridos")
                )
                return@put
            }
            
            val success = userService.updateUserPassword(email, newPassword, password)
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
            val password = call.request.queryParameters["password"]

            if (email == null || password == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "email y contraseña son requeridos"))
                return@delete
            }

            val user = userService.getUserProfile(email)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
            } else {
                val userPasswordHash = userService.getPasswordHashByEmail(email)
                if (userPasswordHash == null || !BCrypt.checkpw(password, userPasswordHash)) {
                    call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "Contraseña incorrecta"))
                    return@delete
                }
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

            val success = userService.updateUserPasswordToken(email, newPassword)
            if (success) {
                call.respond(HttpStatusCode.OK, ApiResponse(true, "Contraseña restablecida correctamente"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al restablecer la contraseña"))
            }
        }
    }
}
