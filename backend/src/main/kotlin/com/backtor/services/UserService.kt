package com.backtor.services

import com.backtor.models.UserLoginRequest
import com.backtor.models.UserRegisterRequest
import com.backtor.models.UserProfile
import com.backtor.models.UserExperienceTable

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

import com.backtor.security.JwtService

import com.backtor.models.UserTable
import com.backtor.models.PasswordResetTable
import com.backtor.models.UserCoursesTable
import com.backtor.models.CourseTable
import com.backtor.models.Course
import org.mindrot.jbcrypt.BCrypt

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import jakarta.mail.*
import jakarta.mail.internet.*
import java.util.*


import org.jetbrains.exposed.sql.Expression

import java.time.LocalDate

import org.jetbrains.exposed.sql.CustomFunction

import org.jetbrains.exposed.sql.javatime.JavaLocalDateColumnType
fun Expression<LocalDateTime>.date(): Expression<LocalDate> =
    CustomFunction("DATE", JavaLocalDateColumnType(), this)
class UserService {
    fun saveUser(user: UserRegisterRequest): UserRegisterRequest {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        transaction {
            UserTable.insert {
                it[email] = user.email
                it[passwordHash] = hashedPassword
                it[username] = user.username
                it[streak] = 0
                it[lastActiveDate] = LocalDateTime.now()
                it[createdAt] = LocalDateTime.now()
            }
        }
        return user
    }

    fun findByEmail(email: String): UserProfile? {
        return transaction {
            val userRow = UserTable.select { UserTable.email eq email }.firstOrNull() ?: return@transaction null

            val userId = userRow[UserTable.id]

            // Calcula experiencia total (ajusta según cómo almacenas experiencia total)
            val experienceTotal = userRow[UserTable.experienceTotal]

            // Calcula experiencia en últimos 7 días (si aplica)
            val sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN)
            val experienceScore = UserExperienceTable
                .slice(UserExperienceTable.experiencePoints)
                .select {
                    (UserExperienceTable.userId eq userId) and
                            (UserExperienceTable.collectedAt greaterEq sevenDaysAgo)
                }
                .sumOf { it[UserExperienceTable.experiencePoints] }

            UserProfile(
                email = userRow[UserTable.email],
                username = userRow[UserTable.username],
                streak = userRow[UserTable.streak],
                experienceScore = experienceScore,
                experienceTotal = experienceTotal,
                lastActivity = userRow[UserTable.lastActiveDate],
                createdAt = userRow[UserTable.createdAt]
            )
        }
    }

    fun findByIdentifier(identifier: String): UserProfile? {
        return transaction {
            val userByEmail = UserTable.select { UserTable.email eq identifier }.firstOrNull()
            val userByUsername = UserTable.select { UserTable.username eq identifier }.firstOrNull()

            val user = userByEmail ?: userByUsername

            if (user != null) {
                val userId = user[UserTable.id]
                val experienceScore = UserExperienceTable
                    .slice(UserExperienceTable.experiencePoints)
                    .select { UserExperienceTable.userId eq userId }
                    .sumOf { it[UserExperienceTable.experiencePoints] }

                val experienceTotal = user[UserTable.experienceTotal]

                UserProfile(
                    email = user[UserTable.email],
                    username = user[UserTable.username],
                    streak = user[UserTable.streak],
                    experienceScore = experienceScore,
                    experienceTotal = experienceTotal,
                    lastActivity = user[UserTable.lastActiveDate],
                    createdAt = user[UserTable.createdAt]
                )
            } else {
                null
            }
        }
    }

    fun deleteUser(email: String) {
        transaction {
            UserTable.deleteWhere { UserTable.email eq email }
        }
    }
    fun getUserProfile(email: String): UserProfile? {
        return transaction {
            val userRow = UserTable.select { UserTable.email eq email }.firstOrNull() ?: return@transaction null

            val userId = userRow[UserTable.id]
            val totalExperience = userRow[UserTable.experienceTotal]

            // Calcular experiencia de los últimos 7 días
            val sevenDaysAgo = LocalDateTime.now().minusDays(7)

            val last7DaysExp = UserExperienceTable
                .slice(UserExperienceTable.experiencePoints)
                .select {
                    (UserExperienceTable.userId eq userId) and
                            (UserExperienceTable.collectedAt greaterEq sevenDaysAgo)
                }
                .sumOf { it[UserExperienceTable.experiencePoints] }

            UserProfile(
                email = userRow[UserTable.email],
                username = userRow[UserTable.username],
                streak = userRow[UserTable.streak],
                experienceScore = last7DaysExp,  // no experience_score
                experienceTotal = totalExperience,
                lastActivity = userRow[UserTable.lastActiveDate],
                createdAt = userRow[UserTable.createdAt]
            )
        }
    }


    fun updateStreak(email: String): Boolean {
        return transaction {
            // Obtener el usuario y su última fecha de actividad
            val userData = UserTable
                .select { UserTable.email eq email }
                .map { 
                    Pair(
                        it[UserTable.streak], 
                        it[UserTable.lastActiveDate].toLocalDate()
                    )
                }
                .firstOrNull() ?: return@transaction false
    
            val (currentStreak, lastActiveDate) = userData
            val today = LocalDateTime.now().toLocalDate()
    
            // Verificar si ya actualizó hoy
            if (lastActiveDate == today) {
                return@transaction false
            }
    
            // Verificar si es un día consecutivo (para mantener el streak)
            val isConsecutiveDay = lastActiveDate.plusDays(1) == today
            
            val newStreak = if (isConsecutiveDay) {
                currentStreak + 1
            } else {
                // Si no es consecutivo, reiniciar a 1
                1
            }
    
            // Actualizar el streak y la última fecha de actividad
            val updatedRows = UserTable.update({ UserTable.email eq email }) {
                it[UserTable.streak] = newStreak
                it[UserTable.lastActiveDate] = LocalDateTime.now()
            }
            
            updatedRows > 0
        }
    }
    fun resetStreak(email: String): Boolean {
        return transaction {
            val updatedRows = UserTable.update({ UserTable.email eq email }) {
                it[UserTable.streak] = 0
                it[UserTable.lastActiveDate] = LocalDateTime.now()
            }
            updatedRows > 0
        }
    }
    fun updateUserPassword(email: String, newPassword: String, password: String): Boolean {
        return transaction {
            // Validar entrada
            if (email.isBlank() || newPassword.isBlank() || password.isBlank()) {
                return@transaction false
            }

            // Verificar si la contraseña actual es correcta
            val hashedPasswordFromDB = UserTable
                .select { UserTable.email eq email }
                .map { it[UserTable.passwordHash] }
                .firstOrNull()

            if (hashedPasswordFromDB != null && BCrypt.checkpw(password, hashedPasswordFromDB)) {
                // Generar hash de la nueva contraseña
                val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())

                // Actualizar la contraseña
                val updatedRows = UserTable.update({ UserTable.email eq email }) {
                    it[UserTable.passwordHash] = hashedPassword
                }
                updatedRows > 0
            } else {
                false // Contraseña incorrecta
            }
        }
    }
    fun getPasswordHashByEmail(email: String): String? {
        return transaction {
            UserTable
                .select { UserTable.email eq email }
                .map { it[UserTable.passwordHash] }
                .firstOrNull()
        }
    }
    fun getPasswordHashByIdentifier(identifier: String): String? {
        return transaction {
            val userByEmail = UserTable.select { UserTable.email eq identifier }.firstOrNull()
            val userByUsername = UserTable.select { UserTable.username eq identifier }.firstOrNull()
            
            val user = userByEmail ?: userByUsername
            user?.get(UserTable.passwordHash)
        }
    }
    fun savePasswordResetToken(email: String, token: Int) {
        // Convertir el token a String y generar su hash
        val hashedToken = BCrypt.hashpw(token.toString(), BCrypt.gensalt())
        println("Token: $token")
        println("Hashed Token: $hashedToken")

        // Insertar en la base de datos
        transaction {
            PasswordResetTable.insert {
                it[PasswordResetTable.email] = email  // Guarda el correo electrónico
                it[PasswordResetTable.token] = hashedToken  // Guarda el hash del token
                it[PasswordResetTable.expiration] = LocalDateTime.now().plusHours(1)  // Fecha de expiración
            }
        }
    }
    fun validatePasswordResetToken(email: String, rawToken: Int): Boolean {
        return transaction {
            val tokenData = PasswordResetTable
                .select { PasswordResetTable.email eq email }
                .map { row -> row[PasswordResetTable.token] to row[PasswordResetTable.expiration] }
                .firstOrNull() ?: return@transaction false

            val (hashedToken, expiration) = tokenData

            // Validar que el hash no sea nulo o inválido
            if (hashedToken.isNullOrBlank()) {
                return@transaction false
            }

            val isTokenValid = try {
                BCrypt.checkpw(rawToken.toString(), hashedToken)
            } catch (e: IllegalArgumentException) {
                false // Hash inválido
            }

            val isNotExpired = expiration.isAfter(LocalDateTime.now())

            isTokenValid && isNotExpired
        }
    }
    fun sendEmail(email: String, subject: String, body: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(
                    "sierra.alejandro@correounivalle.edu.co",
                    "xzuyvzmghslcookc"
                ) // Cambia esto por tu contraseña
            }
        })
    try {
        val bodyHtml = """
            <html>
                <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; background-color: #f9f9f9; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                        <h2 style="color: #4CAF50; text-align: center;">Recuperación de Contraseña</h2>
                        <p>Hola,</p>
                        <p>Hemos recibido una solicitud para restablecer tu contraseña. Aquí tienes tu código:</p>
                        <div style="background: #f0f0f0; padding: 15px; margin: 20px 0; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 1px;">
                            $body
                        </div>
                        <p>Si no solicitaste este cambio, puedes ignorar este correo. Tu contraseña actual seguirá siendo válida.</p>
                        <p style="margin-top: 30px;">Gracias,<br><strong>El equipo de soporte</strong></p>
                        <hr style="margin: 40px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            Este correo fue generado automáticamente. Por favor, no respondas a este mensaje.
                        </p>
                    </div>
                </body>
            </html>
        """.trimIndent()
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress("sierra.alejandro@correounivalle.edu.co"))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            setSubject(subject)
            setContent(bodyHtml, "text/html; charset=utf-8")
        }

        Transport.send(message)
        println("Correo enviado a $email")
    } catch (e: MessagingException) {
        e.printStackTrace()
    }
    }
    fun updateUserUsername(email: String, newUsername: String, currentPassword: String?, newPassword: String?): Boolean {
        return transaction {
            val user = UserTable.select { UserTable.email eq email }.firstOrNull()
            if (user == null) return@transaction false

            // Si se proporciona contraseña actual, verificar que sea correcta
            if (currentPassword != null) {
                val isPasswordValid = BCrypt.checkpw(currentPassword, user[UserTable.passwordHash])
                if (!isPasswordValid) return@transaction false
            }

            // Actualizar username
            UserTable.update({ UserTable.email eq email }) {
                it[UserTable.username] = newUsername
            }

            // Si se proporciona nueva contraseña, actualizarla
            if (newPassword != null) {
                val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                UserTable.update({ UserTable.email eq email }) {
                    it[UserTable.passwordHash] = hashedPassword
                }
            }

            true
        }
    }
    fun updateUserPasswordToken(email: String, newPassword: String): Boolean {
        val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        return transaction {
            val updatedRows = UserTable.update({ UserTable.email eq email }) {
                it[UserTable.passwordHash] = hashedPassword
            }
            updatedRows > 0
        }
    }
    fun loginUser(loginRequest: UserLoginRequest): String? {
        val user = transaction {
            UserTable.select { UserTable.email eq loginRequest.identifier }
                .map { it[UserTable.passwordHash] }
                .firstOrNull()
        }

        return if (user != null && BCrypt.checkpw(loginRequest.password, user)) {
            JwtService.generateToken(loginRequest.identifier)
        } else {
            null
        }
    }
    fun addExperience(email: String, points: Int): Boolean {
        return transaction {
            val user = UserTable.select { UserTable.email eq email }.firstOrNull() ?: return@transaction false
            val userId = user[UserTable.id]
            val today = LocalDate.now()

            // Buscar si ya existe un registro para hoy
            val todayExp = UserExperienceTable
                .select { (UserExperienceTable.userId eq userId) and (UserExperienceTable.collectedAt.date() eq org.jetbrains.exposed.sql.javatime.dateLiteral(today)) }
                .firstOrNull()

            if (todayExp != null) {
                // Sumar puntos al registro existente
                UserExperienceTable.update({ UserExperienceTable.id eq todayExp[UserExperienceTable.id] }) {
                    it[experiencePoints] = todayExp[UserExperienceTable.experiencePoints] + points
                    it[collectedAt] = LocalDateTime.now()
                }
            } else {
                // Obtener los últimos 7 registros de experiencia
                val last7 = UserExperienceTable
                    .select { UserExperienceTable.userId eq userId }
                    .orderBy(UserExperienceTable.collectedAt to org.jetbrains.exposed.sql.SortOrder.DESC)
                    .limit(7)
                    .toList()

                // Si ya hay 7, elimina el más antiguo
                if (last7.size == 7) {
                    val oldest = last7.minByOrNull { it[UserExperienceTable.collectedAt] }
                    if (oldest != null) {
                        UserExperienceTable.deleteWhere { UserExperienceTable.id eq oldest[UserExperienceTable.id] }
                    }
                }

                // Insertar el nuevo registro de experiencia
                UserExperienceTable.insert {
                    it[UserExperienceTable.userId] = userId
                    it[experiencePoints] = points
                    it[collectedAt] = LocalDateTime.now()
                }
            }

            // Actualizar experiencia total
            UserTable.update({ UserTable.id eq userId }) {
                it[experienceTotal] = user[UserTable.experienceTotal] + points
            }

            true
        }
    }
    fun updateExperience(email: String, points: Int): Boolean {
        return transaction {
            val user = UserTable.select { UserTable.email eq email }.firstOrNull() ?: return@transaction false
            val userId = user[UserTable.id]

            // Actualizar experiencia total
            UserTable.update({ UserTable.id eq userId }) {
                it[experienceTotal] = points
            }

            true
        }
    }
    fun getUserProfileFromToken(token: String): UserProfile? {
        val email = JwtService.verifyToken(token) ?: return null
        return getUserProfile(email)
    }
    fun getCoursesByUserEmail(email: String): List<Course> {
        return transaction {
            val userId = UserTable
                .select { UserTable.email eq email }
                .map { it[UserTable.id] }
                .firstOrNull() ?: return@transaction emptyList()
            (UserCoursesTable innerJoin CourseTable)
                .select { UserCoursesTable.userId eq userId }
                .map {
                    Course(
                        id = it[CourseTable.id],
                        title = it[CourseTable.title],
                        description = it[CourseTable.description]
                    )
                }
        }
    }
    fun addCourseToUser(email: String, courseId: Int): Boolean {
        return transaction {
            val userId = UserTable
                .select { UserTable.email eq email }
                .map { it[UserTable.id] }
                .firstOrNull() ?: return@transaction false

            val alreadyExists = UserCoursesTable
                .select { (UserCoursesTable.userId eq userId) and (UserCoursesTable.courseId eq courseId) }
                .count() > 0

            if (alreadyExists) return@transaction false

            UserCoursesTable.insert {
                it[UserCoursesTable.userId] = userId
                it[UserCoursesTable.courseId] = courseId
            }
            true
        }
    }
}
