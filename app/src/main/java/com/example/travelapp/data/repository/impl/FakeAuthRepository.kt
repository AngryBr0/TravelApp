package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.User
import com.example.travelapp.data.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * FakeAuthRepository — временная реализация AuthRepository.
 *
 * Сейчас мы еще не подключаем Firebase.
 * Этот класс имитирует вход, регистрацию и выход пользователя.
 *
 * Позже мы заменим его на FirebaseAuthRepository,
 * но ViewModel и экраны менять не придется,
 * потому что они зависят от интерфейса AuthRepository.
 */
class FakeAuthRepository : AuthRepository {

    /**
     * Текущий пользователь приложения.
     *
     * Пока это просто переменная в памяти.
     * В Firebase-версии текущий пользователь будет браться из FirebaseAuth.
     */
    private var currentUser: User? = null

    /**
     * Имитация входа пользователя.
     *
     * override означает, что мы реализуем функцию,
     * объявленную в интерфейсе AuthRepository.
     */
    override suspend fun signIn(
        email: String,
        password: String
    ): AppResult<User> {
        delay(500) // имитируем задержку сети

        if (email.isBlank() || password.isBlank()) {
            return AppResult.Error("Введите email и пароль")
        }

        val user = User(
            id = "fake-user-id",
            email = email,
            name = "Пользователь"
        )

        currentUser = user

        return AppResult.Success(user)
    }

    /**
     * Имитация регистрации пользователя.
     */
    override suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): AppResult<User> {
        delay(500)

        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return AppResult.Error("Заполните все поля")
        }

        val user = User(
            id = "fake-user-id",
            email = email,
            name = name
        )

        currentUser = user

        return AppResult.Success(user)
    }

    /**
     * Имитация выхода из аккаунта.
     */
    override suspend fun signOut(): AppResult<Unit> {
        currentUser = null
        return AppResult.Success(Unit)
    }

    /**
     * Возвращает id текущего пользователя.
     *
     * Если пользователь не вошел, вернется null.
     */
    override fun getCurrentUserId(): String? {
        return currentUser?.id
    }
}