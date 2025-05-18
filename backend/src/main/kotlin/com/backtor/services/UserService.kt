package com.backtor.services

import com.backtor.models.UserLoginRequest
import com.backtor.models.UserRegisterRequest
import com.backtor.models.UserProfile

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.and

import com.backtor.security.JwtService

import com.backtor.models.UserTable
import com.backtor.models.PasswordResetTable
import org.mindrot.jbcrypt.BCrypt

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import jakarta.mail.*
import jakarta.mail.internet.*
import java.util.*

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
            UserTable.select { UserTable.email eq email }
                .map {
                    UserProfile(
                        email = it[UserTable.email],
                        username = it[UserTable.username],
                        streak = it[UserTable.streak],
                        lastActivity = it[UserTable.lastActiveDate],
                        createdAt = it[UserTable.createdAt]
                    )
                }.firstOrNull()
        }
    }
    fun deleteUser(email: String) {
        transaction {
            UserTable.deleteWhere { UserTable.email eq email }
        }
    }
    fun getUserProfile(email: String): UserProfile? {
        return transaction {
            UserTable.select { UserTable.email eq email }
                .map {
                    UserProfile(
                        email = it[UserTable.email],
                        username = it[UserTable.username],
                        streak = it[UserTable.streak],
                        lastActivity = it[UserTable.lastActiveDate],
                        createdAt = it[UserTable.createdAt]
                    )
                }.firstOrNull()
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
    fun updateUserUsername(email: String, newUsername: String, password: String, confirmPassword: String): Boolean {
        return transaction {
            // Verificar si la contraseña es correcta
            val hashedPassword = UserTable
                .select { UserTable.email eq email }
                .map { it[UserTable.passwordHash] }
                .firstOrNull()

            if (hashedPassword != null && BCrypt.checkpw(password, hashedPassword)) {
                if (password != confirmPassword) {
                    return@transaction false // Las contraseñas no coinciden
                }
                // Actualizar el nombre de usuario
                val updatedRows = UserTable.update({ UserTable.email eq email }) {
                    it[UserTable.username] = newUsername
                }
                updatedRows > 0
            } else {
                false // Contraseña incorrecta
            }
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
            UserTable.select { UserTable.email eq loginRequest.email }
                .map { it[UserTable.passwordHash] }
                .firstOrNull()
        }

        return if (user != null && BCrypt.checkpw(loginRequest.password, user)) {
            JwtService.generateToken(loginRequest.email)
        } else {
            null
        }
    }


}
