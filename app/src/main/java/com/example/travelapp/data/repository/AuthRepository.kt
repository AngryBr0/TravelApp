package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.User

/**
 * AuthRepository — интерфейс для работы с авторизацией.
 *
 * Интерфейс описывает, какие операции авторизации нужны приложению:
 * вход, регистрация, выход и получение текущего пользователя.
 *
 * ViewModel работает именно с этим интерфейсом, а не напрямую с Firebase.
 */
interface AuthRepository {

    /**
     * Вход пользователя по email и паролю.
     */
    suspend fun signIn(
        email: String,
        password: String
    ): AppResult<User>

    /**
     * Регистрация нового пользователя.
     */
    suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): AppResult<User>

    /**
     * Выход из аккаунта.
     */
    suspend fun signOut(): AppResult<Unit>

    /**
     * Получить id текущего пользователя.
     *
     * Если пользователь не авторизован, вернется null.
     */
    fun getCurrentUserId(): String?

    /**
     * Получить объект текущего пользователя.
     *
     * Это нужно для экрана профиля:
     * там мы показываем email, имя и id пользователя.
     */
    fun getCurrentUser(): User?
}