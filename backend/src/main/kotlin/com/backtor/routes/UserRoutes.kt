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
    }
}
