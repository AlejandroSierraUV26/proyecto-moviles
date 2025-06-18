package com.backtor.services

import com.backtor.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.net.URLEncoder
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.util.concurrent.TimeUnit
import org.jetbrains.exposed.sql.insert
import java.time.DateTimeException

class ExamService {
    suspend fun generateAndSaveCoursePreview(topic: String, apiKey: String, createdBy: String): Pair<Int, String> {
        val prompt = """
        Genera un JSON VÁLIDO y COMPLETO sobre "$topic" con:
        - 3 secciones (básica=1, intermedia=2, avanzada=3)
        - 3 preguntas por sección(SOLO 2 palabras por pregunta)
        - 4 opciones por pregunta (SOLO 1 palabra por opción, TODAS entre comillas)
        - Estructura EXACTA:

        {
          "title": "Título del curso",
          "description": "Descripción",
          "sections": [
            {
              "title": "Básico",
              "difficultyLevel": 1,
              "questions": [
                {
                  "questionText": "Pregunta?",
                  "options": ["Op1", "Op2", "Op3", "Op4"],
                  "correctAnswer": "Op3",
                  "feedback": "Explicación simple sin comillas internas"
                }
              ]
            }
          ]
        }
    """.trimIndent()

        val requestJson = buildJsonObject {
            put("model", "gpt-3.5-turbo-0125")
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
            put("response_format", buildJsonObject { put("type", "json_object") })
        }

        val body = Json.encodeToString(requestJson).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.aimlapi.com/chat/completions")
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Error en la API: ${response.code}")
        }

        val responseBody = response.body?.string() ?: throw Exception("Respuesta vacía")

        // Guardar en la tabla de previews con el created_by
        val previewId = transaction {
            // Obtener el ID del usuario como entero
            val userId = UserTable
                .select { UserTable.email eq createdBy }
                .map { it[UserTable.id] }
                .firstOrNull() ?: throw Exception("Usuario no encontrado")

            CoursePreviews.insert {
                it[this.topic] = topic
                it[jsonContent] = responseBody
                it[this.createdBy] = userId // Asignamos el ID del usuario
            }[CoursePreviews.id]
        }

        return Pair(previewId, responseBody)
    }

    suspend fun processPreviewToCourse(previewId: Int, createdBy: String): Int {
        val preview = transaction {
            CoursePreviews.select { CoursePreviews.id eq previewId }
                .singleOrNull()
        } ?: throw Exception("Preview no encontrado")

        val jsonContent = preview[CoursePreviews.jsonContent]

        val contentRaw = Json.parseToJsonElement(jsonContent).jsonObject
            .get("choices")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
            ?: throw Exception("No se pudo extraer contenido")

        val generated = Json { ignoreUnknownKeys = true }.decodeFromString<GenerateCourse>(contentRaw)

        return transaction {
            // Obtener el ID del usuario como entero
            val userId = UserTable
                .select { UserTable.email eq createdBy }
                .map { it[UserTable.id] } // Obtenemos el valor entero del ID
                .firstOrNull() ?: throw Exception("Usuario no encontrado")

            // Crear el curso asignando directamente el ID del usuario
            val courseId = CourseTable.insert {
                it[CourseTable.title] = generated.title
                it[CourseTable.description] = generated.description
                it[CourseTable.createdBy] = userId // Asignamos el ID del usuario como entero
            }[CourseTable.id]

            // Resto de la lógica para crear secciones, exámenes y preguntas
            generated.sections.forEach { section ->
                val sectionId = SectionTable.insert {
                    it[SectionTable.courseId] = courseId
                    it[SectionTable.title] = section.title
                    it[SectionTable.difficultyLevel] = section.difficultyLevel
                }[SectionTable.id]

                val examId = ExamTable.insert {
                    it[ExamTable.title] = "Examen de ${section.title}"
                    it[ExamTable.description] = "Evalúa conocimientos de ${section.title}"
                    it[ExamTable.sectionId] = sectionId
                    it[ExamTable.difficultyLevel] = section.difficultyLevel
                }[ExamTable.id]

                section.questions.forEach { question ->
                    QuestionTable.insert {
                        it[QuestionTable.examId] = examId
                        it[QuestionTable.questionText] = question.questionText
                        it[QuestionTable.options] = question.options.joinToString("||")
                        it[QuestionTable.correctAnswer] = question.correctAnswer
                        it[QuestionTable.feedback] = question.feedback
                    }
                }
            }

            courseId
        }
    }

    // ─────────────── CURSOS ───────────────
    fun createCourse(request: CourseRequest): Int = transaction {
        CourseTable.insert {
            it[title] = request.title
            it[description] = request.description
        }[CourseTable.id]
    }
    fun getAllCourses(userEmail: String? = null): List<Course> = transaction {
        // Obtener el ID del usuario si existe
        val userId = userEmail?.let { email ->
            UserTable.select { UserTable.email eq email }
                .map { it[UserTable.id] }
                .firstOrNull()
        }

        // Consulta base
        val query = when (userId) {
            null -> CourseTable.select { CourseTable.createdBy.isNull() } // Solo cursos públicos
            else -> CourseTable.select {
                (CourseTable.createdBy.isNull()) or
                        (CourseTable.createdBy eq userId)
            } // Cursos públicos + del usuario
        }

        query.map {
            Course(
                id = it[CourseTable.id],
                title = it[CourseTable.title],
                description = it[CourseTable.description],
                createdBy = it[CourseTable.createdBy] // Agrega este campo al modelo Course
            )
        }
    }
    fun updateCourse(id: Int, request: CourseRequest): Boolean = transaction {
        CourseTable.update({ CourseTable.id eq id }) {
            it[title] = request.title
            it[description] = request.description
        } > 0
    }
    fun deleteCourse(id: Int): Boolean = transaction {
        CourseTable.deleteWhere { CourseTable.id eq id } > 0
    }
    // ─────────────── SECCIONES ───────────────
    fun createSection(request: SectionRequest): Int = transaction {
        val exists = CourseTable.select { CourseTable.id eq request.courseId }.count() > 0
        if (!exists) throw IllegalArgumentException("Curso no encontrado")
        SectionTable.insert {
            it[courseId] = request.courseId
            it[title] = request.title
            it[difficultyLevel] = request.difficultyLevel
        }[SectionTable.id]
    }
    fun getSectionsByCourse(courseId: Int): List<Section> = transaction {
        SectionTable.select { SectionTable.courseId eq courseId }.map {
            Section(it[SectionTable.id], it[SectionTable.courseId], it[SectionTable.title], it[SectionTable.difficultyLevel])
        }
    }
    fun getAllSections(): List<Section> = transaction {
        SectionTable.selectAll().map {
            Section(it[SectionTable.id], it[SectionTable.courseId], it[SectionTable.title], it[SectionTable.difficultyLevel])
        }
    }
    fun updateSection(id: Int, request: SectionRequest): Boolean = transaction {
        val exists = CourseTable.select { CourseTable.id eq request.courseId }.count() > 0
        if (!exists) throw IllegalArgumentException("Curso no encontrado")
        SectionTable.update({ SectionTable.id eq id }) {
            it[courseId] = request.courseId
            it[title] = request.title
            it[difficultyLevel] = request.difficultyLevel
        } > 0
    }
    fun deleteSection(id: Int): Boolean = transaction {
        SectionTable.deleteWhere { SectionTable.id eq id } > 0
    }
    // ─────────────── EXÁMENES ───────────────
    fun createExam(request: ExamRequest): Int = transaction {
        val sectionExists = SectionTable.select { SectionTable.id eq request.sectionId }.count() > 0
        if (!sectionExists) throw IllegalArgumentException("Sección no encontrada")
        ExamTable.insert {
            it[title] = request.title
            it[description] = request.description
            it[sectionId] = request.sectionId
            it[difficultyLevel] = request.difficultyLevel
        }[ExamTable.id]
    }
    fun getExamById(id: Int): Exam? = transaction {
        ExamTable.select { ExamTable.id eq id }.map {
            Exam(
                id = it[ExamTable.id],
                title = it[ExamTable.title],
                description = it[ExamTable.description],
                sectionId = it[ExamTable.sectionId],
                difficultyLevel = it[ExamTable.difficultyLevel]
            )
        }.singleOrNull()
    }
    fun getExamsBySection(sectionId: Int): List<Exam> = transaction {
        ExamTable.select { ExamTable.sectionId eq sectionId }.map {
            Exam(
                id = it[ExamTable.id],
                title = it[ExamTable.title],
                description = it[ExamTable.description],
                sectionId = it[ExamTable.sectionId],
                difficultyLevel = it[ExamTable.difficultyLevel]
            )
        }
    }
    fun getAllExams(): List<Exam> = transaction {
        ExamTable.selectAll().map {
            Exam(
                id = it[ExamTable.id],
                title = it[ExamTable.title],
                description = it[ExamTable.description],
                sectionId = it[ExamTable.sectionId],
                difficultyLevel = it[ExamTable.difficultyLevel]
            )
        }
    }
    fun updateExam(id: Int, request: ExamRequest): Boolean = transaction {
        val sectionExists = SectionTable.select { SectionTable.id eq request.sectionId }.count() > 0
        if (!sectionExists) throw IllegalArgumentException("Sección no encontrada")
        ExamTable.update({ ExamTable.id eq id }) {
            it[title] = request.title
            it[description] = request.description
            it[sectionId] = request.sectionId
            it[difficultyLevel] = request.difficultyLevel
        } > 0
    }
    fun deleteExam(id: Int): Boolean = transaction {
        ExamTable.deleteWhere { ExamTable.id eq id } > 0
    }
    //─────────────── RECOMENDACIONES ───────────────
    private fun generateScholarRecommendation(topic: String): List<ResourceRecommendation> {
        val encoded = URLEncoder.encode(topic, "UTF-8")
        val url = "https://scholar.google.com/scholar?q=$encoded"
        return listOf(ResourceRecommendation("Buscar en Google Scholar", url, "Google Scholar"))
    }
    private fun generateYoutubeRecommendation(topic: String): List<ResourceRecommendation> {
        val encoded = URLEncoder.encode(topic, "UTF-8")
        val url = "https://www.youtube.com/results?search_query=$encoded&sp=CAM%253D" // Filtro: orden por vistas

        return listOf(
            ResourceRecommendation("Ver videos populares en YouTube", url, "YouTube")
        )
    }
    private fun getMotivationalMessage(score: Int): String {
        return if (score < 70) {
            "¡No te rindas! Aunque no alcanzaste el 70%, te dejamos recursos para reforzar y dominar el tema. ¡Tú puedes!"
        } else {
            "¡Felicidades! Superaste el reto. Sigue estudiando con ese mismo entusiasmo, ¡vas por excelente camino!"
        }
    }
    private fun generateRecommendationsForTopic(topic: String): List<ResourceRecommendation> {
        return generateYoutubeRecommendation(topic) + generateScholarRecommendation(topic)
    }
    // ─────────────── PREGUNTAS ───────────────
    fun createQuestion(request: QuestionRequest): Int = transaction {
        val exam = ExamTable.select {
            (ExamTable.sectionId eq request.sectionId) and
                    (ExamTable.difficultyLevel eq request.difficultyLevel)
        }.singleOrNull() ?: throw IllegalArgumentException("No existe un examen para esa sección y dificultad")
        if (request.options.size < 2 || request.correctAnswer !in request.options)
            throw IllegalArgumentException("Opciones inválidas o respuesta incorrecta fuera de opciones")
        QuestionTable.insert {
            it[examId] = exam[ExamTable.id]
            it[questionText] = request.questionText
            it[options] = request.options.joinToString("||")
            it[correctAnswer] = request.correctAnswer
            it[feedback] = request.feedback
        }[QuestionTable.id]
    }
    fun getQuestionsByExam(examId: Int): List<Question> = transaction {
        QuestionTable.select { QuestionTable.examId eq examId }.map {
            Question(
                id = it[QuestionTable.id],
                examId = it[QuestionTable.examId],
                questionText = it[QuestionTable.questionText],
                options = it[QuestionTable.options].split("||"),
                correctAnswer = it[QuestionTable.correctAnswer],
                feedback = it[QuestionTable.feedback]
            )
        }
    }
    fun getAllQuestions(): List<Question> = transaction {
        QuestionTable.selectAll().map {
            Question(
                id = it[QuestionTable.id],
                examId = it[QuestionTable.examId],
                questionText = it[QuestionTable.questionText],
                options = it[QuestionTable.options].split("||"),
                correctAnswer = it[QuestionTable.correctAnswer],
                feedback = it[QuestionTable.feedback]
            )
        }
    }
    fun updateQuestion(id: Int, request: QuestionRequest): Boolean = transaction {
        val exam = ExamTable.select {
            (ExamTable.sectionId eq request.sectionId) and
                    (ExamTable.difficultyLevel eq request.difficultyLevel)
        }.singleOrNull() ?: throw IllegalArgumentException("No existe un examen para esa sección y dificultad")

        if (request.options.size < 2 || request.correctAnswer !in request.options)
            throw IllegalArgumentException("Opciones inválidas o respuesta incorrecta fuera de opciones")
        QuestionTable.update({ QuestionTable.id eq id }) {
            it[examId] = exam[ExamTable.id]
            it[questionText] = request.questionText
            it[options] = request.options.joinToString("||")
            it[correctAnswer] = request.correctAnswer
            it[feedback] = request.feedback
        } > 0
    }
    fun deleteQuestion(id: Int): Boolean = transaction {
        QuestionTable.deleteWhere { QuestionTable.id eq id } > 0
    }
    // ─────────────── EVALUACIÓN ───────────────
    fun evaluateExam(submission: ExamSubmission): ExamFeedbackResult = transaction {
        val examId = submission.examId

        val examRow = ExamTable.select { ExamTable.id eq examId }.firstOrNull()
            ?: throw IllegalArgumentException("Examen no encontrado")

        val examTitle = examRow[ExamTable.title]

        val feedbackList = submission.answers.mapNotNull { answer ->
            val row = QuestionTable.select {
                (QuestionTable.id eq answer.questionId) and
                        (QuestionTable.examId eq examId)
            }.firstOrNull() ?: return@mapNotNull null

            val correct = row[QuestionTable.correctAnswer]
            val options = row[QuestionTable.options].split("||")

            AnswerFeedback(
                questionId = answer.questionId,
                questionText = row[QuestionTable.questionText],
                options = options,
                selectedAnswer = answer.selectedAnswer,
                correctAnswer = correct,
                isCorrect = correct == answer.selectedAnswer
            )
        }
        val correct = feedbackList.count { it.isCorrect }
        val total = feedbackList.size
        val percentage = if (total > 0) (correct * 100) / total else 0
        val recommendations = if (percentage < 70) {
            generateRecommendationsForTopic(examTitle)
        } else emptyList()
        val motivationalMessage = getMotivationalMessage(percentage)
        ExamFeedbackResult(
            feedbackList = feedbackList,
            correct = correct,
            total = total,
            percentage = percentage,
            recommendations = recommendations,
            motivationalMessage = motivationalMessage
        )
    }
    // ─────────────── PROGRESO DEL USUARIO ───────────────
    fun saveExamProgress(email: String, submission: ExamSubmission, result: ExamFeedbackResult): Boolean = transaction {
        val userId = UserTable
            .select { UserTable.email eq email }
            .map { it[UserTable.id] }
            .firstOrNull() ?: return@transaction false

        val existingProgress = UserExamProgressTable
            .select {
                (UserExamProgressTable.userId eq userId) and
                        (UserExamProgressTable.examId eq submission.examId)
            }
            .firstOrNull()

        val scorePercentage = result.percentage
        val isCompleted = scorePercentage >= 70 || (existingProgress?.get(UserExamProgressTable.completed) == true)
        if (existingProgress == null) {
            UserExamProgressTable.insert {
                it[UserExamProgressTable.userId] = userId
                it[UserExamProgressTable.examId] = submission.examId
                it[UserExamProgressTable.questionsAnswered] = result.total
                it[UserExamProgressTable.questionsCorrect] = result.correct
                it[UserExamProgressTable.completed] = isCompleted
                it[UserExamProgressTable.lastAttemptDate] = LocalDateTime.now()
                it[UserExamProgressTable.bestScore] = scorePercentage
            }
        } else {
            UserExamProgressTable.update({
                (UserExamProgressTable.userId eq userId) and
                        (UserExamProgressTable.examId eq submission.examId)
            }) {
                it[UserExamProgressTable.questionsAnswered] = existingProgress[UserExamProgressTable.questionsAnswered] + result.total
                it[UserExamProgressTable.questionsCorrect] = existingProgress[UserExamProgressTable.questionsCorrect] + result.correct
                it[UserExamProgressTable.completed] = isCompleted
                it[UserExamProgressTable.lastAttemptDate] = LocalDateTime.now()
                it[UserExamProgressTable.bestScore] = maxOf(existingProgress[UserExamProgressTable.bestScore], scorePercentage)
            }
        }
        updateCourseProgress(userId, submission.examId)
        true
    }
    private fun updateCourseProgress(userId: Int, examId: Int) = transaction {
        // Obtener el curso asociado a este examen
        val courseId = (ExamTable innerJoin SectionTable)
            .select { ExamTable.id eq examId }
            .map { it[SectionTable.courseId] }
            .firstOrNull() ?: return@transaction
        // Obtener todas las secciones del curso
        val sections = SectionTable
            .select { SectionTable.courseId eq courseId }
            .map { it[SectionTable.id] }
        // Obtener todos los exámenes del curso
        val exams = ExamTable
            .select { ExamTable.sectionId inList sections }
            .map { it[ExamTable.id] }
        // Calcular progreso del curso
        val progress = calculateCourseProgress(userId, exams)
        // Actualizar o insertar en user_courses
        val existingRecord = UserCoursesTable
            .select {
                (UserCoursesTable.userId eq userId) and
                        (UserCoursesTable.courseId eq courseId)
            }
            .firstOrNull()
        if (existingRecord == null) {
            UserCoursesTable.insert {
                it[UserCoursesTable.userId] = userId
                it[UserCoursesTable.courseId] = courseId
                it[progressPercentage] = progress
            }
        } else {
            UserCoursesTable.update({
                (UserCoursesTable.userId eq userId) and
                        (UserCoursesTable.courseId eq courseId)
            }) {
                it[progressPercentage] = progress
            }
        }
    }
    private fun calculateCourseProgress(userId: Int, examIds: List<Int>): Int = transaction {
        if (examIds.isEmpty()) return@transaction 0
        // Obtener el progreso de todos los exámenes del curso
        val examProgresses = UserExamProgressTable
            .select {
                (UserExamProgressTable.userId eq userId) and
                        (UserExamProgressTable.examId inList examIds)
            }
            .map { it[UserExamProgressTable.examId] to it[UserExamProgressTable.completed] }
        // Calcular porcentaje de exámenes completados
        val completedExams = examProgresses.count { it.second }
        val totalExams = examIds.size
        ((completedExams * 100) / totalExams).coerceIn(0, 100)
    }
    fun getCourseProgress(email: String, courseId: Int): CourseProgressResponse = transaction {
        val userId = UserTable
            .select { UserTable.email eq email }
            .map { it[UserTable.id] }
            .firstOrNull() ?: return@transaction CourseProgressResponse(0, emptyList())
        // Obtener todas las secciones del curso
        val sections = SectionTable
            .select { SectionTable.courseId eq courseId }
            .map { sectionRow ->
                val sectionId = sectionRow[SectionTable.id]

                // Obtener todos los exámenes de esta sección
                val exams = ExamTable
                    .select { ExamTable.sectionId eq sectionId }
                    .map { examRow ->
                        val examId = examRow[ExamTable.id]
                        // Obtener progreso del usuario para este examen
                        val progress = UserExamProgressTable
                            .select {
                                (UserExamProgressTable.userId eq userId) and
                                        (UserExamProgressTable.examId eq examId)
                            }
                            .firstOrNull()
                        ExamProgress(
                            examId = examId,
                            title = examRow[ExamTable.title],
                            completed = progress?.get(UserExamProgressTable.completed) ?: false,
                            bestScore = progress?.get(UserExamProgressTable.bestScore) ?: 0,
                            lastAttemptDate = progress?.get(UserExamProgressTable.lastAttemptDate)
                                ?.let { LocalDateTimeWrapper(it) }
                        )
                    }

                SectionProgress(
                    sectionId = sectionId,
                    title = sectionRow[SectionTable.title],
                    exams = exams,
                    completedExams = exams.count { it.completed },
                    totalExams = exams.size
                )
            }
        // Calcular progreso general del curso
        val totalCompletedExams = sections.sumOf { it.completedExams }
        val totalExams = sections.sumOf { it.totalExams }
        val courseProgress = if (totalExams > 0) (totalCompletedExams * 100) / totalExams else 0
        CourseProgressResponse(courseProgress, sections)
    }
    fun getDiagnosticQuestions(courseId: Int, maxLevel: Int): List<DiagnosticQuestion> = transaction {
        val sections = SectionTable
            .select {
                (SectionTable.courseId eq courseId) and
                        (SectionTable.difficultyLevel lessEq maxLevel)
            }
            .map { it[SectionTable.id] }

        if (sections.isEmpty()) return@transaction emptyList()

        val exams = ExamTable
            .select { ExamTable.sectionId inList sections }
            .map { it[ExamTable.id] }

        if (exams.isEmpty()) return@transaction emptyList()

        QuestionTable
            .select { QuestionTable.examId inList exams }
            .map {
                DiagnosticQuestion(
                    id = it[QuestionTable.id],
                    examId = it[QuestionTable.examId],
                    questionText = it[QuestionTable.questionText],
                    options = it[QuestionTable.options].split("||"),
                    correctAnswer = it[QuestionTable.correctAnswer], // Incluir respuesta correcta
                    difficultyLevel = ExamTable
                        .select { ExamTable.id eq it[QuestionTable.examId] }
                        .first()[ExamTable.difficultyLevel]
                )
            }
    }
    fun evaluateDiagnosticSubmission(email: String, submission: DiagnosticSubmission): DiagnosticFeedback = transaction {
        val userId = UserTable
            .select { UserTable.email eq email }
            .map { it[UserTable.id] }
            .firstOrNull() ?: throw IllegalArgumentException("Usuario no encontrado")

        val allQuestions = getDiagnosticQuestions(submission.courseId, submission.maxLevel)
        if (allQuestions.isEmpty()) {
            throw IllegalArgumentException("No se encontraron preguntas para este curso y nivel")
        }

        val questionsByLevel = allQuestions.groupBy { it.difficultyLevel }
        val results = mutableListOf<DiagnosticResult>()
        var recommendedStartingSection: String? = null
        var isCourseCompleted = false

        // Evaluar cada nivel
        for (level in 1..submission.maxLevel) {
            val levelQuestions = questionsByLevel[level] ?: emptyList()

            if (levelQuestions.isEmpty()) {
                results.add(DiagnosticResult(level, false, 0.0, null, "No hay preguntas para este nivel"))
                continue
            }

            val answeredQuestions = levelQuestions.filter { question ->
                submission.answers.containsKey(question.id)
            }

            if (answeredQuestions.isEmpty()) {
                results.add(DiagnosticResult(level, false, 0.0, null, "No respondiste preguntas de este nivel"))
                continue
            }

            val correctAnswers = answeredQuestions.count { question ->
                submission.answers[question.id] == question.correctAnswer
            }

            val score = (correctAnswers.toDouble() / answeredQuestions.size) * 100
            val passed = score >= 80.0

            val sectionTitle = SectionTable
                .select {
                    (SectionTable.courseId eq submission.courseId) and
                            (SectionTable.difficultyLevel eq level)
                }
                .limit(1)
                .map { it[SectionTable.title] }
                .firstOrNull()

            results.add(
                DiagnosticResult(
                    levelTested = level,
                    passed = passed,
                    score = score,
                    startingSection = sectionTitle,
                    message = if (passed)
                        "¡Aprobaste el nivel $level con ${"%.1f".format(score)}%!"
                    else
                        "Necesitas mejorar en el nivel $level (${"%.1f".format(score)}%)"
                )
            )

            if (!passed && recommendedStartingSection == null) {
                recommendedStartingSection = sectionTitle
            }
        }

        // Verificar si completó el curso
        val passedAll = results.all { it.passed }
        val maxLevelInCourse = SectionTable
            .select { SectionTable.courseId eq submission.courseId }
            .maxByOrNull { it[SectionTable.difficultyLevel] }
            ?.get(SectionTable.difficultyLevel) ?: 0

        isCourseCompleted = passedAll && submission.maxLevel >= maxLevelInCourse

        // Actualizar progreso de exámenes
        val examIds = allQuestions.map { it.examId }.distinct()
        for (examId in examIds) {
            val examQuestions = allQuestions.filter { it.examId == examId }
            val answeredExamQuestions = examQuestions.filter { submission.answers.containsKey(it.id) }

            if (answeredExamQuestions.isNotEmpty()) {
                val examCorrect = answeredExamQuestions.count { question ->
                    submission.answers[question.id] == question.correctAnswer
                }

                val examScore = (examCorrect.toDouble() / answeredExamQuestions.size) * 100
                val isExamCompleted = examScore >= 70.0

                // Manejar inserción o actualización
                val existingProgress = UserExamProgressTable
                    .select {
                        (UserExamProgressTable.userId eq userId) and
                                (UserExamProgressTable.examId eq examId)
                    }
                    .firstOrNull()

                if (existingProgress == null) {
                    UserExamProgressTable.insert {
                        it[UserExamProgressTable.userId] = userId
                        it[UserExamProgressTable.examId] = examId
                        it[UserExamProgressTable.questionsAnswered] = answeredExamQuestions.size
                        it[UserExamProgressTable.questionsCorrect] = examCorrect
                        it[UserExamProgressTable.completed] = isExamCompleted
                        it[UserExamProgressTable.lastAttemptDate] = LocalDateTime.now()
                        it[UserExamProgressTable.bestScore] = examScore.toInt()
                    }
                } else {
                    UserExamProgressTable.update({
                        (UserExamProgressTable.userId eq userId) and
                                (UserExamProgressTable.examId eq examId)
                    }) {
                        it[UserExamProgressTable.questionsAnswered] = existingProgress[UserExamProgressTable.questionsAnswered] + answeredExamQuestions.size
                        it[UserExamProgressTable.questionsCorrect] = existingProgress[UserExamProgressTable.questionsCorrect] + examCorrect
                        it[UserExamProgressTable.completed] = isExamCompleted || existingProgress[UserExamProgressTable.completed]
                        it[UserExamProgressTable.lastAttemptDate] = LocalDateTime.now()
                        it[UserExamProgressTable.bestScore] = maxOf(existingProgress[UserExamProgressTable.bestScore], examScore.toInt())
                    }
                }

                updateCourseProgress(userId, examId)
            }
        }

        // Marcar curso como completado si corresponde
        if (isCourseCompleted) {
            UserCoursesTable.update({
                (UserCoursesTable.userId eq userId) and
                        (UserCoursesTable.courseId eq submission.courseId)
            }) {
                it[UserCoursesTable.progressPercentage] = 100
                it[UserCoursesTable.completed] = true
            }
        }

        // Mensaje final
        val overallMessage = when {
            isCourseCompleted -> "¡Felicidades! Has completado todo el curso."
            passedAll && submission.maxLevel < maxLevelInCourse -> "¡Bien hecho! Has aprobado todos los niveles evaluados. Continúa con el nivel ${submission.maxLevel + 1}."
            passedAll -> "¡Felicidades! Dominas todos los niveles evaluados."
            else -> recommendedStartingSection?.let { "Recomendamos comenzar con: $it" }
                ?: "Por favor revisa tus resultados."
        }

        // Si completó el curso, no se recomienda ninguna sección
        val finalRecommendedSection = if (isCourseCompleted) null
        else recommendedStartingSection ?: results.lastOrNull()?.startingSection ?: "No se pudo determinar"

        DiagnosticFeedback(
            results = results,
            overallResult = overallMessage,
            recommendedStartingSection = finalRecommendedSection,
            isCourseCompleted = isCourseCompleted
        )

    }
}
