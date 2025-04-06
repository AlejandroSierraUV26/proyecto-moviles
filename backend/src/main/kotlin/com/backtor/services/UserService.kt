package com.backtor.services

import com.backtor.models.User
import com.backtor.models.UserRegisterRequest

class UserService {
    private val users = mutableListOf<User>()

    fun findByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    fun saveUser(userRequest: UserRegisterRequest) {
        val user = User(email = userRequest.email, password = userRequest.password)
        users.add(user)
    }
}
