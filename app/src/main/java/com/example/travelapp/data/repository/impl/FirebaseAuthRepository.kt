package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.User
import com.example.travelapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseAuthRepository — реальная реализация AuthRepository через Firebase.
 *
 * Этот класс заменяет FakeAuthRepository.
 *
 * Он отвечает за:
 * - регистрацию пользователя;
 * - вход пользователя;
 * - выход из аккаунта;
 * - получение текущего пользователя.
 *
 * ViewModel не знает, что внутри используется Firebase.
 * Она продолжает работать через интерфейс AuthRepository.
 */
class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    /**
     * Вход пользователя по email и паролю.
     *
     * signInWithEmailAndPassword возвращает Task.
     * await() превращает Task в suspend-операцию,
     * чтобы работать с Firebase через корутины.
     */
    override suspend fun signIn(
        email: String,
        password: String
    ): AppResult<User> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return AppResult.Error("Введите email и пароль")
            }

            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return AppResult.Error("Не удалось получить пользователя")

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                name = firebaseUser.displayName.orEmpty()
            )

            AppResult.Success(user)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка входа"
            )
        }
    }

    /**
     * Регистрация нового пользователя.
     *
     * После создания аккаунта:
     * 1. обновляем displayName в FirebaseAuth;
     * 2. сохраняем пользователя в коллекцию users в Firestore.
     */
    override suspend fun signUp(
        email: String,
        password: String,
        name: String
    ): AppResult<User> {
        return try {
            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                return AppResult.Error("Заполните все поля")
            }

            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return AppResult.Error("Не удалось создать пользователя")

            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }

            firebaseUser.updateProfile(profileUpdates).await()

            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                name = name
            )

            /**
             * users — коллекция пользователей в Firestore.
             *
             * document(user.id) означает, что id документа будет
             * совпадать с uid пользователя из FirebaseAuth.
             */
            firestore
                .collection("users")
                .document(user.id)
                .set(user)
                .await()

            AppResult.Success(user)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка регистрации"
            )
        }
    }

    /**
     * Выход из аккаунта.
     */
    override suspend fun signOut(): AppResult<Unit> {
        return try {
            firebaseAuth.signOut()
            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка выхода из аккаунта"
            )
        }
    }

    /**
     * Получает id текущего пользователя.
     *
     * Если пользователь не вошел, currentUser будет null.
     */
    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Получает текущего пользователя из FirebaseAuth.
     *
     * Это синхронная функция, поэтому здесь берём только те данные,
     * которые FirebaseAuth уже хранит локально: uid, email, displayName.
     */
    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null

        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            name = firebaseUser.displayName.orEmpty()
        )
    }
}