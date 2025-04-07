package com.backtor.services

import com.backtor.models.UserLoginRequest
import com.backtor.models.UserRegisterRequest

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

import com.backtor.models.UserTable
import org.mindrot.jbcrypt.BCrypt

import java.time.LocalDateTime
import java.util.UUID


class UserService {
    fun saveUser(user: UserRegisterRequest) {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        transaction {
            UserTable.insert {
                it[email] = user.email
                it[passwordHash] = hashedPassword
                it[username] = user.username
                it[description] = user.description
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }
    fun findByEmail(email: String): Boolean {
        return transaction {
            UserTable.select { UserTable.email eq email }
                .count() > 0
        }
    }
    fun deleteUser(email: String) {
        transaction {
            UserTable.deleteWhere { UserTable.email eq email }
        }
    }
}
