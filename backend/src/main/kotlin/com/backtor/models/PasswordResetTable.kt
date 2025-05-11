package com.backtor.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.UUID

object PasswordResetTable : Table("password_reset_tokens") {
    val email = varchar("user_email", 255)
    val token = varchar("token",60)
    val expiration = datetime("expires_at")
}