package com.backtor.routes

import com.backtor.models.UserRegisterRequest
import com.backtor.models.ApiResponse
import com.backtor.services.UserService

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

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
    }
}
