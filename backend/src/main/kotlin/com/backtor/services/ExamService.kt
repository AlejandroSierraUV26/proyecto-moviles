package com.backtor.services

import com.backtor.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

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
        val examExists = ExamTable.select { ExamTable.id eq examId }.count() > 0
        if (!examExists) throw IllegalArgumentException("Examen no encontrado")
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
        ExamFeedbackResult(feedbackList, correct, total, percentage)
    }
}
