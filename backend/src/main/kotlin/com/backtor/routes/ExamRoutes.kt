package com.backtor.routes

import com.backtor.models.*
import com.backtor.services.*
import com.backtor.services.ExamService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.auth.*
import com.backtor.security.JwtService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.authenticate



fun Route.examRoutes() {
    val examService = ExamService()
    route("/api/exams") {
        // Endpoint para generar preview
        /*post("/generate-preview") {
            val request = call.receive<Map<String, String>>()
            val topic = request["topic"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Tema requerido")
            val apiKey = "7647a357c35f411daaacc715f2d416a4" // Deberías usar variables de entorno

            try {
                val (previewId, jsonResponse) = examService.generateAndSaveCoursePreview(topic, apiKey)
                call.respond(HttpStatusCode.OK, mapOf(
                    "status" to "success",
                    "previewId" to previewId,
                    "preview" to jsonResponse,
                    "message" to "Preview del curso generado y guardado temporalmente"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "status" to "error",
                    "message" to "Error generando preview: ${e.message}"
                ))
            }
        }

// Endpoint para procesar el preview a curso completo
        post("/process-preview") {
            val request = call.receive<Map<String, String>>() // Cambiado a String
            val previewId = request["previewId"]?.toIntOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "ID de preview requerido o inválido")
                )

            try {
                val courseId = examService.processPreviewToCourse(previewId)
                call.respond(HttpStatusCode.OK, mapOf(
                    "status" to "success",
                    "courseId" to courseId,
                    "message" to "Curso generado exitosamente"
                ))
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SerializationException -> "Error en el formato del curso: ${e.message}"
                    else -> "Error procesando preview: ${e.message}"
                }

                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "status" to "error",
                    "message" to errorMessage
                ))
            }
        }*/

        // CURSOS
        post("/courses") {
            val request = call.receive<CourseRequest>()
            val id = examService.createCourse(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        }
        put("/courses/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid course ID")
            val request = call.receive<CourseRequest>()
            val updated = examService.updateCourse(id, request)
            if (updated) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        delete("/courses/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid course ID")
            val deleted = examService.deleteCourse(id)
            if (deleted) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        // SECCIONES
        post("/sections") {
            val request = call.receive<SectionRequest>()
            val id = examService.createSection(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        }
        get("/sections/{courseId}") {
            val courseId = call.parameters["courseId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid courseId")
            call.respond(examService.getSectionsByCourse(courseId))
        }
        get("/sections") {
            call.respond(examService.getAllSections())
        }
        put("/sections/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid section ID")
            val request = call.receive<SectionRequest>()
            val updated = examService.updateSection(id, request)
            if (updated) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        delete("/sections/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid section ID")
            val deleted = examService.deleteSection(id)
            if (deleted) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        // EXÁMENES
        post("/exams") {
            val request = call.receive<ExamRequest>()
            val id = examService.createExam(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        }
        get("/exams/{examId}") {
            val examId = call.parameters["examId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid examId")
            val exam = examService.getExamById(examId) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(exam)
        }
        get("/by-section/{sectionId}") {
            val sectionId = call.parameters["sectionId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid sectionId")
            println("API LOG: sectionId recibido = $sectionId")
            val exams = examService.getExamsBySection(sectionId)
            println("API LOG: exámenes encontrados = ${exams.size}")
            call.respond(HttpStatusCode.OK, exams)
        }
        get("/exams") {
            call.respond(examService.getAllExams())
        }
        put("/exams/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid exam ID")
            val request = call.receive<ExamRequest>()
            val updated = examService.updateExam(id, request)
            if (updated) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        delete("/exams/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid exam ID")
            val deleted = examService.deleteExam(id)
            if (deleted) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        // PREGUNTAS
        post("/questions") {
            val request = call.receive<QuestionRequest>()
            val id = examService.createQuestion(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        }
        get("/questions/{examId}") {
            val examId = call.parameters["examId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid examId")
            call.respond(examService.getQuestionsByExam(examId))
        }
        get("/questions") {
            call.respond(examService.getAllQuestions())
        }
        put("/questions/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid question ID")
            val request = call.receive<QuestionRequest>()
            val updated = examService.updateQuestion(id, request)
            if (updated) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        delete("/questions/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid question ID")
            val deleted = examService.deleteQuestion(id)
            if (deleted) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
        // ENVÍO DE RESPUESTAS PARA EVALUAR EXAMEN
        // PROGRESO DEL USUARIO
        authenticate("auth-jwt") {
            get("/courses") {
                val email = call.getEmailFromToken() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(false, "No autorizado")
                )

                call.respond(examService.getAllCourses(email))
            }
            post("/generate-preview") {
                val email = call.getEmailFromToken() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("status" to "error", "message" to "No autorizado")
                )

                val request = call.receive<Map<String, String>>()
                val topic = request["topic"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("status" to "error", "message" to "Tema requerido")
                )

                val apiKey = "7647a357c35f411daaacc715f2d416a4" // Deberías usar variables de entorno

                try {
                    val (previewId, jsonResponse) = examService.generateAndSaveCoursePreview(
                        topic = topic,
                        apiKey = apiKey,
                        createdBy = email // Pasamos el email del usuario autenticado
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "status" to "success",
                        "previewId" to previewId,
                        "preview" to jsonResponse,
                        "message" to "Preview del curso generado y guardado temporalmente"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "status" to "error",
                        "message" to "Error generando preview: ${e.message}"
                    ))
                }
            }

            // Procesar preview a curso completo
            post("/process-preview") {
                val email = call.getEmailFromToken() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("status" to "error", "message" to "No autorizado")
                )

                val request = call.receive<Map<String, String>>()
                val previewId = request["previewId"]?.toIntOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("status" to "error", "message" to "ID de preview requerido o inválido")
                    )

                try {
                    val courseId = examService.processPreviewToCourse(
                        previewId = previewId,
                        createdBy = email // Pasamos el email del usuario como creador
                    )

                    call.respond(HttpStatusCode.OK, mapOf(
                        "status" to "success",
                        "courseId" to courseId,
                        "message" to "Curso generado exitosamente"
                    ))
                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is NoSuchElementException -> "Preview no encontrado"
                        is SerializationException -> "Error en el formato del curso: ${e.message}"
                        else -> "Error procesando preview: ${e.message}"
                    }

                    call.respond(HttpStatusCode.InternalServerError, mapOf(
                        "status" to "error",
                        "message" to errorMessage
                    ))
                }
            }
            post("/diagnostic") {
                val email = call.getEmailFromToken() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(false, "No autorizado")
                )
                val submission = call.receive<DiagnosticSubmission>()
                // Validar que el nivel sea uno de los permitidos
                if (!listOf("basic", "intermediate", "advanced").contains(submission.level.toLowerCase())) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "Nivel no válido. Use 'basic', 'intermediate' o 'advanced'")
                    )
                }
                val (startingSection, hasIncompleteSection) = examService.evaluateDiagnosticQuiz(email, submission)
                call.respond(HttpStatusCode.OK, mapOf(
                    "success" to true,
                    "startingSection" to startingSection,
                    "hasIncompleteSection" to hasIncompleteSection,
                    "levelTested" to submission.level,
                    "message" to if (hasIncompleteSection)
                        "Según tu nivel ${submission.level}, debes comenzar en la sección $startingSection"
                    else "¡Felicidades! Dominas todo el nivel ${submission.level} del curso"
                ))
            }
            get("/progress/{courseId}") {
                val email = call.getEmailFromToken() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse(false, "No autorizado")
                )

                val courseId = call.parameters["courseId"]?.toIntOrNull() ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "ID de curso inválido")
                )

                val progress = examService.getCourseProgress(email, courseId)
                call.respond(HttpStatusCode.OK, progress)
            }
        }
        post("/submit") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            val email = JwtService.verifyToken(token ?: "") ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val submission = call.receive<ExamSubmission>()
            val result = examService.evaluateExam(submission)
            val saved = examService.saveExamProgress(email, submission, result)
            if (saved) call.respond(result)
            else call.respond(HttpStatusCode.InternalServerError, "No se pudo guardar el progreso")
        }
    }
}
