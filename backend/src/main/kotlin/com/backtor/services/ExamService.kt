package com.backtor.services

import com.backtor.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.net.URLEncoder

class ExamService {
    // ─────────────── CURSOS ───────────────
    fun createCourse(request: CourseRequest): Int = transaction {
        CourseTable.insert {
            it[title] = request.title
            it[description] = request.description
        }[CourseTable.id]
    }
    fun getAllCourses(): List<Course> = transaction {
        CourseTable.selectAll().map {
            Course(
                id = it[CourseTable.id],
                title = it[CourseTable.title],
                description = it[CourseTable.description]
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
    fun evaluateDiagnosticQuiz(email: String, submission: DiagnosticSubmission): Pair<String, Boolean> = transaction {
        val userId = UserTable
            .select { UserTable.email eq email }
            .map { it[UserTable.id] }
            .firstOrNull() ?: throw IllegalArgumentException("Usuario no encontrado")

        // Determinar los niveles a evaluar según la selección del usuario
        val levelsToEvaluate = when (submission.level.toLowerCase()) {
            "basic" -> listOf(1)
            "intermediate" -> listOf(2, 3)
            "advanced" -> listOf(4)
            else -> throw IllegalArgumentException("Nivel no válido. Use 'basic', 'intermediate' o 'advanced'")
        }
        val sections = SectionTable
            .select {
                (SectionTable.courseId eq submission.courseId) and
                        (SectionTable.difficultyLevel inList levelsToEvaluate)
            }
            .orderBy(SectionTable.difficultyLevel to SortOrder.ASC)
            .toList()
        var startingSectionTitle = "Nivel completo"
        var hasIncompleteSection = false
        for (section in sections) {
            val sectionId = section[SectionTable.id]
            val exams = ExamTable.select { ExamTable.sectionId eq sectionId }.toList()
            var totalQuestions = 0
            var correctAnswers = 0
            for (exam in exams) {
                val examId = exam[ExamTable.id]
                val questions = QuestionTable.select { QuestionTable.examId eq examId }
                for (q in questions) {
                    val qid = q[QuestionTable.id]
                    val userAnswer = submission.answers[qid]
                    val correctAnswer = q[QuestionTable.correctAnswer]
                    if (userAnswer != null) {
                        totalQuestions++
                        if (userAnswer == correctAnswer) correctAnswers++
                    }
                }
                val scorePercentage = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
                val isCompleted = scorePercentage >= 70
                UserExamProgressTable.insert {
                    it[UserExamProgressTable.userId] = userId
                    it[UserExamProgressTable.examId] = examId
                    it[questionsAnswered] = totalQuestions
                    it[questionsCorrect] = correctAnswers
                    it[completed] = isCompleted
                    it[lastAttemptDate] = LocalDateTime.now()
                    it[bestScore] = scorePercentage
                }
            }
            val sectionScore = if (totalQuestions == 0) 0.0 else (correctAnswers.toDouble() / totalQuestions) * 100
            if (sectionScore < 70.0 && !hasIncompleteSection) {
                startingSectionTitle = section[SectionTable.title]
                hasIncompleteSection = true
            }
        }
        // Actualizar progreso general del curso solo para las secciones evaluadas
        updateCourseProgress(userId, submission.courseId)
        startingSectionTitle to hasIncompleteSection
    }

}
