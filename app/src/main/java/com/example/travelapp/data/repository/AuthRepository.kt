package com.example.travelapp.data.repository
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.User

interface AuthRepository {

    suspend fun signIn(
        email: String,
        password: String
    ): AppResult<User>

    suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): AppResult<User>

    suspend fun signOut(): AppResult<Unit>

    fun getCurrentUserId(): String?
}