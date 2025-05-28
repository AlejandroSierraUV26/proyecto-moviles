package com.backtor.routes

import com.backtor.models.UserRegisterRequest
import com.backtor.models.UserLoginRequest
import com.backtor.models.ApiResponse
import com.backtor.services.UserService

import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

import io.ktor.server.auth.*


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

import io.ktor.server.application.*
import kotlinx.coroutines.launch

import org.mindrot.jbcrypt.BCrypt
import com.backtor.security.JwtService


// Crea instancia del servicio
val userService = UserService()
val jwtService = JwtService

fun ApplicationCall.getEmailFromToken(): String? =
    this.principal<JWTPrincipal>()?.getClaim("email", String::class)

fun Route.userRoutes() {
    route("/api") {
        post("/register"){
            val userRequest = call.receive<UserRegisterRequest>()

            if (userService.findByEmail(userRequest.email) != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse(success = false, message = "El correo electrónico ya está registrado")
                )
                return@post
            }

            try {
                val newUser = userService.saveUser(userRequest)

                val token = jwtService.generateToken(newUser.email)

                call.respond(
                    HttpStatusCode.Created,
                    mapOf(
                        "token" to token,
                        "message" to "Usuario registrado correctamente"
                    )
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
        post("/login"){
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

            val token = jwtService.generateToken(user.email)

            call.respond(
                HttpStatusCode.OK,
                mapOf("token" to token)
            )
        }
        post("/password/forgot") {
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
            launch {
                userService.sendEmail(email, "Recupera tu contraseña", "Tu token es: $token")
            }

            call.respond(HttpStatusCode.OK, ApiResponse(true, "Se ha enviado un correo con las instrucciones"))
        }
        post("/password/reset") {
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
        post("/profile") {
            val request = call.receive<Map<String, String>>()
            val username = request["username"] ?: ""
            val currentPassword = request["currentPassword"]
            val newPassword = request["newPassword"]
            val confirmPassword = request["confirmPassword"]

            // Validar que el username no esté vacío
            if (username.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "success" to false,
                    "message" to "El nombre de usuario no puede estar vacío"
                ))
                return@post
            }

            // Si se proporciona nueva contraseña, validar todos los campos
            if (newPassword != null || confirmPassword != null) {
                if (currentPassword == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "Debe proporcionar su contraseña actual"
                    ))
                    return@post
                }
                if (newPassword == null || confirmPassword == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "Debe proporcionar tanto la nueva contraseña como su confirmación"
                    ))
                    return@post
                }
                if (newPassword != confirmPassword) {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "Las contraseñas no coinciden"
                    ))
                    return@post
                }
            }

            try {
                val userEmail = call.principal<JWTPrincipal>()?.getClaim("email", String::class)
                if (userEmail == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf(
                        "success" to false,
                        "message" to "No autorizado"
                    ))
                    return@post
                }

                val result = userService.updateUserUsername(userEmail, username, currentPassword, newPassword)
                if (result) {
                    call.respond(mapOf(
                        "success" to true,
                        "message" to "Perfil actualizado correctamente"
                    ))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf(
                        "success" to false,
                        "message" to "Error al actualizar el perfil"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "success" to false,
                    "message" to "Error del servidor: ${e.message}"
                ))
            }
        }
        authenticate("auth-jwt") {
            get("/profile") {
                val email = call.getEmailFromToken()
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
            put("/profile/update") {
                val email = call.getEmailFromToken()
                if (email == null) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                    return@put
                }

                val request = try {
                    call.receive<Map<String, String>>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Datos inválidos"))
                    return@put
                }

                val newUsername = request["username"]
                val currentPassword = request["currentPassword"]
                val newPassword = request["newPassword"]
                val confirmPassword = request["confirmPassword"]

                if (newUsername.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Nombre de usuario es requerido"))
                    return@put
                }

                // Si se proporciona una nueva contraseña, validar que coincida con la confirmación
                if (!newPassword.isNullOrEmpty()) {
                    if (confirmPassword.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Confirmación de contraseña es requerida"))
                        return@put
                    }
                    if (newPassword != confirmPassword) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Las contraseñas no coinciden"))
                        return@put
                    }
                    if (currentPassword.isNullOrEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Debe proporcionar su contraseña actual"))
                        return@put
                    }
                }

                val result = userService.updateUserUsername(
                    email = email,
                    newUsername = newUsername,
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                if (result) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Perfil actualizado correctamente"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al actualizar el perfil"))
                }
            }
            put("/password/update") {
                val request = try {
                    call.receive<Map<String, String>>()
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "Datos inválidos")
                    )
                    return@put
                }

                val email = call.getEmailFromToken()
                val currentPassword = request["currentPassword"]
                val newPassword = request["newPassword"]
                val confirmPassword = request["confirmPassword"]

                if (email == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "No autorizado")
                    )
                    return@put
                }

                if (currentPassword.isNullOrEmpty() || newPassword.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "Todos los campos son requeridos")
                    )
                    return@put
                }

                if (newPassword != confirmPassword) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "Las contraseñas no coinciden")
                    )
                    return@put
                }

                val success = userService.updateUserPassword(email, newPassword, currentPassword)
                if (success) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, "Contraseña actualizada correctamente")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "Contraseña actual incorrecta")
                    )
                }
            }
            delete("/delete") {
                val email = call.getEmailFromToken()
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
            put("/streak/update") {
                val email = call.getEmailFromToken()


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
            put("/streak/reset"){
                val email = call.getEmailFromToken()
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
            put("/experience/update") {
                val email = call.getEmailFromToken()
                val request = try {
                    call.receive<Map<String, String>>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Datos inválidos"))
                    return@put
                }
                val score = request["score"]?.toIntOrNull()
                if (email == null) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Email es requerido"))
                    return@put
                }
                val user = userService.getUserProfile(email)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
                } else {
                    userService.updateExperience(email, score ?: 0)
                    userService.addExperience(email, score ?: 0)
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Experiencia actualizada"))
                }

            }
            get("/courses") {
                val email = call.getEmailFromToken()
                if (email == null) {
                    call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "No autorizado"))
                    return@get
                }

                val courses = userService.getCoursesByUserEmail(email)
                call.respond(HttpStatusCode.OK, courses)
            }
            post("/courses/add") {
                val email = call.getEmailFromToken()
                if (email == null) {
                    call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "No autorizado"))
                    return@post
                }

                val request = try {
                    call.receive<Map<String, String>>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Formato de solicitud inválido"))
                    return@post
                }

                val courseIdString = request["courseId"]
                if (courseIdString.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "courseId es requerido"))
                    return@post
                }

                val courseId = try {
                    courseIdString.toInt()
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "courseId inválido"))
                    return@post
                }


                val success = userService.addCourseToUser(email, courseId)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Curso agregado exitosamente"))
                } else {
                    call.respond(HttpStatusCode.Conflict, ApiResponse(false, "Ya estás inscrito en este curso o no existe"))
                }
            }
            post("/courses/remove") {
                val email = call.getEmailFromToken()
                if (email == null) {
                    call.respond(HttpStatusCode.Unauthorized, ApiResponse(false, "No autorizado"))
                    return@post
                }

                val request = try {
                    call.receive<Map<String, String>>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Formato de solicitud inválido"))
                    return@post
                }

                val courseIdString = request["courseId"]
                if (courseIdString.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "courseId es requerido"))
                    return@post
                }

                val courseId = try {
                    courseIdString.toInt()
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "courseId inválido"))
                    return@post
                }

                val success = userService.deleteCourseFromUser(email, courseId)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Curso eliminado exitosamente"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Curso no encontrado o no estás inscrito en él"))
                }
            }


        }
    }
}

