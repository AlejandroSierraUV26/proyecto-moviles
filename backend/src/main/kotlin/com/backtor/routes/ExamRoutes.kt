package com.backtor.routes

import com.backtor.models.*
import com.backtor.services.ExamService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

val examService = ExamService()

fun Route.examRoutes() {
    route("/api/exams") {
        // Categories
        //CREAR CATEGORIA: http://localhost:8080/api/exams/categories
        post("/categories") {
            try {
                val request = call.receive<CategoryRequest>()
                val categoryId = examService.createCategory(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponseWithData(true, "Categoría creada", categoryId)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "Error al crear categoría: ${e.message}")
                )
            }
        }
        //OBTENER TODAS LAS CATEGORIAS: http://localhost:8080/api/exams/categories
        get("/categories") {
            try {
                val categories = examService.getAllCategories()
                if (categories.isEmpty()) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(false, "No hay categorías disponibles")
                    )
                } else {
                    call.respond(categories)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al obtener categorías: ${e.message}")
                )
            }
        }
        //ACTUALIZAR LOS DATOS DE UNA CATEGORIA: http://localhost:8080/api/exams/categories/{id_categoria}
        put("/categories/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "ID de categoría inválido"))
                    return@put
                }
                val request = call.receive<CategoryRequest>()
                val updated = examService.updateCategory(id, request)
                if (updated) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Categoría actualizada"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Categoría no encontrada"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al actualizar categoría: ${e.message}"))
            }
        }
        //ELIMINAR CATEGORIA: http://localhost:8080/api/exams/categories/{id_categoria}
        delete("/categories/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "ID de categoría inválido"))
                    return@delete
                }
                val deleted = examService.deleteCategory(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Categoría eliminada"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Categoría no encontrada"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al eliminar categoría: ${e.message}"))
            }
        }
        // Courses
        //CREAR UN CURSO ASOCIADO A UNA CATEGORIA: http://localhost:8080/api/exams/courses
        post("/courses") {
            try {
                val request = call.receive<CourseRequest>()
                val courseId = examService.createCourse(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponseWithData(true, "Curso creado", courseId)
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, e.message ?: "Datos inválidos")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al crear curso: ${e.message}")
                )
            }
        }
        //OBTENER TODOS LOS CURSOS: http://localhost:8080/api/exams/courses
        get("/courses") {
            try {
                val courses = examService.getAllCourses()
                if (courses.isEmpty()) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(false, "No hay cursos disponibles")
                    )
                } else {
                    call.respond(courses)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al obtener cursos: ${e.message}")
                )
            }
        }
        //OBTENER CURSOS DE UNA CATEGORIA: http://localhost:8080/api/exams/courses/{category_id}
        get("/courses/{categoryId}") {
            try {
                val categoryId = call.parameters["categoryId"]?.toIntOrNull() ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "ID de categoría inválido")
                    )
                    return@get
                }

                val courses = examService.getCoursesByCategory(categoryId)
                if (courses.isEmpty()) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "No hay cursos para esta categoría")
                    )
                } else {
                    call.respond(courses)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al obtener cursos: ${e.message}")
                )
            }
        }
        //ACTUALIZAR LOS DATOS DE UN CURSO EXISTENTE: http://localhost:8080/api/exams/courses/{id_curso}
        put("/courses/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "ID de curso inválido"))
                    return@put
                }
                val request = call.receive<CourseRequest>()
                val updated = examService.updateCourse(id, request)
                if (updated) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Curso actualizado"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Curso no encontrado"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, e.message ?: "Datos inválidos"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al actualizar curso: ${e.message}"))
            }
        }
        //ELIMINAR CURSO: http://localhost:8080/api/exams/courses/{id_curso}
        delete("/courses/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "ID de curso inválido"))
                    return@delete
                }
                val deleted = examService.deleteCourse(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Curso eliminado"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Curso no encontrado"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al eliminar curso: ${e.message}"))
            }
        }
        // Questions
        //CREAR PREGUNTA: http://localhost:8080/api/exams/questions
        post("/questions") {
            try {
                val request = call.receive<QuestionRequest>()
                val questionId = examService.createQuestion(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponseWithData(true, "Pregunta creada", questionId)
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, e.message ?: "Datos de pregunta inválidos")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al crear pregunta: ${e.message}")
                )
            }
        }
        // OBTENER TODAS LAS PREGUNTAS: http://localhost:8080/api/exams/questions
        get("/questions") {
            try {
                val questions = examService.getAllQuestions()
                if (questions.isEmpty()) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(false, "No hay preguntas disponibles")
                    )
                } else {
                    call.respond(questions)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al obtener preguntas: ${e.message}")
                )
            }
        }
        //OBTENER PREGUNTA SEGUN EL CURSO: http://localhost:8080/api/exams/questions/{courseId}
        get("/questions/{courseId}") {
            try {
                val courseId = call.parameters["courseId"]?.toIntOrNull() ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "ID de curso inválido")
                    )
                    return@get
                }

                val questions = examService.getQuestionsByCourse(courseId)
                if (questions.isEmpty()) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "No hay preguntas para este curso")
                    )
                } else {
                    call.respond(questions)
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "Error al obtener preguntas: ${e.message}")
                )
            }
        }
        //ACTUALIZAR PREGUNTA: http://localhost:8080/api/exams/questions/{id_pregunta}
        put("/questions/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "ID de pregunta inválido"))
                    return@put
                }
                val request = call.receive<QuestionRequest>()
                val updated = examService.updateQuestion(id, request)
                if (updated) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Pregunta actualizada"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Pregunta no encontrada"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(false, e.message ?: "Datos de pregunta inválidos"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al actualizar pregunta: ${e.message}"))
            }
        }
        //ELIMINAR PREGUNTA: http://localhost:8080/api/exams/questions/{id}
        delete("/questions/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse(false, "ID de pregunta inválido"))
                    return@delete
                }
                val deleted = examService.deleteQuestion(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, ApiResponse(true, "Pregunta eliminada"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Pregunta no encontrada"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse(false, "Error al eliminar pregunta: ${e.message}"))
            }
        }
    }
}