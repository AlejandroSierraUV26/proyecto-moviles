package com.backtor.models

import org.jetbrains.exposed.sql.Table

object ExamTable : Table("exams") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 100)
    val description = text("description").nullable()
    val sectionId = integer("section_id").references(SectionTable.id) // el examen se asocia a una sección
    val difficultyLevel = integer("difficulty_level") // repetir dificultad para asegurar relación
    override val primaryKey = PrimaryKey(id)
}
