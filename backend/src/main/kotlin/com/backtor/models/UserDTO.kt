package com.backtor.models
import org.jetbrains.exposed.sql.ResultRow
import com.backtor.models.UserTable
import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val username: String,
    val profileImageUrl: String?,
    val streak: Int,
    val experienceTotal: Int
)

fun toUserDTO(row: ResultRow, userAlias: Alias<UserTable>): UserDTO {
    return UserDTO(
        id = row[userAlias[UserTable.id]],
        username = row[userAlias[UserTable.username]],
        profileImageUrl = row[userAlias[UserTable.profileImageUrl]],
        streak = row[userAlias[UserTable.streak]],
        experienceTotal = row[userAlias[UserTable.experienceTotal]]

    )
}