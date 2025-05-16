package com.backtor.services

import com.backtor.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class ExamService {
    // Categories
    fun createCategory(request: CategoryRequest): Int = transaction {
        CategoryTable.insert {
            it[title] = request.title
        }[CategoryTable.id]
    }
    fun getAllCategories(): List<Category> = transaction {
        CategoryTable.selectAll().map {
            Category(
                id = it[CategoryTable.id],
                title = it[CategoryTable.title]
            )
        }
    }
    fun updateCategory(id: Int, request: CategoryRequest): Boolean = transaction {
        CategoryTable.update({ CategoryTable.id eq id }) {
            it[title] = request.title
        } > 0
    }
    fun deleteCategory(id: Int): Boolean = transaction {
        CategoryTable.deleteWhere { CategoryTable.id eq id } > 0
    }
    // Courses
    fun createCourse(request: CourseRequest): Int = transaction {
        val categoryExists = CategoryTable.select {
            CategoryTable.id eq request.categoryId
        }.count() > 0
        if (!categoryExists) {
            throw IllegalArgumentException("La categoría no existe")
        }
        CourseTable.insert {
            it[categoryId] = request.categoryId
            it[title] = request.title
            it[description] = request.description
        }[CourseTable.id]
    }
    fun getAllCourses(): List<Course> = transaction {
        CourseTable.selectAll().map {
            Course(
                id = it[CourseTable.id],
                categoryId = it[CourseTable.categoryId],
                title = it[CourseTable.title],
                description = it[CourseTable.description]
            )
        }
    }
    fun getCoursesByCategory(categoryId: Int): List<Course> = transaction {
        CourseTable.select { CourseTable.categoryId eq categoryId }.map {
            Course(
                id = it[CourseTable.id],
                categoryId = it[CourseTable.categoryId],
                title = it[CourseTable.title],
                description = it[CourseTable.description]
            )
        }
    }
    fun updateCourse(id: Int, request: CourseRequest): Boolean = transaction {
        // Verificar que la categoría exista
        val categoryExists = CategoryTable.select {
            CategoryTable.id eq request.categoryId
        }.count() > 0

        if (!categoryExists) {
            throw IllegalArgumentException("La categoría no existe")
        }
        CourseTable.update({ CourseTable.id eq id }) {
            it[categoryId] = request.categoryId
            it[title] = request.title
            it[description] = request.description
        } > 0
    }
    fun deleteCourse(id: Int): Boolean = transaction {
        QuestionTable.deleteWhere { QuestionTable.courseId eq id }
        CourseTable.deleteWhere { CourseTable.id eq id } > 0
    }
    // Questions
    fun createQuestion(request: QuestionRequest): Int = transaction {
        if (request.options.size < 2) {
            throw IllegalArgumentException("Debe haber al menos 2 opciones")
        }
        if (!request.options.contains(request.correctAnswer)) {
            throw IllegalArgumentException("La respuesta correcta debe estar entre las opciones")
        }
        val courseExists = CourseTable.select {
            CourseTable.id eq request.courseId
        }.count() > 0
        if (!courseExists) {
            throw IllegalArgumentException("El curso no existe")
        }
        QuestionTable.insert {
            it[courseId] = request.courseId
            it[difficultyLevel] = request.difficultyLevel
            it[questionText] = request.questionText
            it[options] = request.options.joinToString("||")
            it[correctAnswer] = request.correctAnswer
        }[QuestionTable.id]
    }
    fun getAllQuestions(): List<Question> = transaction {
        QuestionTable.selectAll().map {
            Question(
                id = it[QuestionTable.id],
                courseId = it[QuestionTable.courseId],
                difficultyLevel = it[QuestionTable.difficultyLevel],
                questionText = it[QuestionTable.questionText],
                options = it[QuestionTable.options]?.split("||") ?: emptyList(),
                correctAnswer = it[QuestionTable.correctAnswer]
            )
        }
    }
    fun getQuestionsByCourse(courseId: Int): List<Question> = transaction {
        QuestionTable.select { QuestionTable.courseId eq courseId }.map {
            Question(
                id = it[QuestionTable.id],
                courseId = it[QuestionTable.courseId],
                difficultyLevel = it[QuestionTable.difficultyLevel],
                questionText = it[QuestionTable.questionText],
                options = it[QuestionTable.options]?.split("||") ?: emptyList(), // Null-safety
                correctAnswer = it[QuestionTable.correctAnswer]
            )
        }
    }
    fun updateQuestion(id: Int, request: QuestionRequest): Boolean = transaction {
        if (request.options.size < 2) {
            throw IllegalArgumentException("Debe haber al menos 2 opciones")
        }

        if (!request.options.contains(request.correctAnswer)) {
            throw IllegalArgumentException("La respuesta correcta debe estar entre las opciones")
        }
        val courseExists = CourseTable.select {
            CourseTable.id eq request.courseId
        }.count() > 0

        if (!courseExists) {
            throw IllegalArgumentException("El curso no existe")
        }
        QuestionTable.update({ QuestionTable.id eq id }) {
            it[courseId] = request.courseId
            it[difficultyLevel] = request.difficultyLevel
            it[questionText] = request.questionText
            it[options] = request.options.joinToString("||")
            it[correctAnswer] = request.correctAnswer
        } > 0
    }
    fun deleteQuestion(id: Int): Boolean = transaction {
        QuestionTable.deleteWhere { QuestionTable.id eq id } > 0
    }
}