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

import com.backtor.models.UserTable
import org.mindrot.jbcrypt.BCrypt

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class UserService {
    fun saveUser(user: UserRegisterRequest) {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        transaction {
            UserTable.insert {
                it[email] = user.email
                it[passwordHash] = hashedPassword
                it[username] = user.username
                it[streak] = 0
                it[lastActiveDate] = LocalDateTime.now() // o LocalDate.now() si usas tipo DATE
                it[createdAt] = LocalDateTime.now()
            }
        }
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

    fun updateUserProfile(email: String, username: String): Boolean {
        return transaction {
            val updatedRows = UserTable.update({ UserTable.email eq email }) {
                it[UserTable.username] = username
            }
            updatedRows > 0
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
                it[streak] = newStreak
                it[lastActiveDate] = LocalDateTime.now()
            }
            
            updatedRows > 0
        }
    }
    fun resetStreak(email: String): Boolean {
        return transaction {
            val updatedRows = UserTable.update({ UserTable.email eq email }) {
                it[streak] = 0
                it[lastActiveDate] = LocalDateTime.now()
            }
            updatedRows > 0
        }
    }

    fun updateUserPassword(email: String, newPassword: String): Boolean {
        val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        return transaction {
            val updatedRows = UserTable.update({ UserTable.email eq email }) {
                it[passwordHash] = hashedPassword
            }
            updatedRows > 0
        }
    }

}
