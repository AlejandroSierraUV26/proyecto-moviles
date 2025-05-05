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
}
