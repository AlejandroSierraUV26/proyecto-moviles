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
            val existingUser = userService.findByEmail(userRequest.email)

            if (existingUser) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse(success = false, message = "El usuario ya existe")
                )
                return@post
            }

            userService.saveUser(userRequest)
            call.respond(
                HttpStatusCode.Created,
                ApiResponse(success = true, message = "Usuario registrado correctamente")
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

        put("/profile") {
            val params = call.receive<Map<String, String>>()
            val email = params["email"]
            val username = params["username"]
            val description = params["description"]

            if (email == null || username == null || description == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "Faltan campos"))
                return@put
            }

            val updated = userService.updateUserProfile(email, username, description)
            if (updated) {
                call.respond(ApiResponse(true, "Perfil actualizado"))
            } else {
                call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Usuario no encontrado"))
            }
        }
    }
}
