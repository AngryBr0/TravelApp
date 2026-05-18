package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.User
import com.example.travelapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
                getLoginErrorMessage(exception)
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
                getRegisterErrorMessage(exception)
            )
        }
    }
    /**
     * Обновляет имя и email пользователя.
     *
     * Имя обновляется в FirebaseAuth displayName и в Firestore users.
     * Email обновляется в FirebaseAuth и в Firestore.
     */
    override suspend fun updateProfile(
        name: String,
        email: String
    ): AppResult<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
                ?: return AppResult.Error("Пользователь не авторизован")

            val normalizedName = name.trim()
            val normalizedEmail = email.trim().lowercase()

            if (normalizedName.isBlank()) {
                return AppResult.Error("Введите имя")
            }

            if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
                return AppResult.Error("Введите корректный email")
            }

            val profileUpdates = userProfileChangeRequest {
                displayName = normalizedName
            }

            firebaseUser.updateProfile(profileUpdates).await()

            if (firebaseUser.email.orEmpty().lowercase() != normalizedEmail) {
                firebaseUser.updateEmail(normalizedEmail).await()
            }

            val updatedUser = User(
                id = firebaseUser.uid,
                email = normalizedEmail,
                name = normalizedName
            )

            firestore
                .collection("users")
                .document(updatedUser.id)
                .set(updatedUser)
                .await()

            AppResult.Success(updatedUser)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка обновления профиля"
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

    private fun getLoginErrorMessage(
        exception: Exception
    ): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                "Неверный email или пароль"
            }

            is FirebaseAuthInvalidUserException -> {
                "Пользователь с таким email не найден"
            }

            is FirebaseNetworkException -> {
                "Проблема с подключением к интернету"
            }

            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> {
                        "Введите корректный email"
                    }

                    "ERROR_WRONG_PASSWORD",
                    "ERROR_INVALID_CREDENTIAL" -> {
                        "Неверный email или пароль"
                    }

                    "ERROR_USER_NOT_FOUND" -> {
                        "Пользователь с таким email не найден"
                    }

                    "ERROR_USER_DISABLED" -> {
                        "Аккаунт пользователя отключён"
                    }

                    "ERROR_TOO_MANY_REQUESTS" -> {
                        "Слишком много попыток входа. Попробуйте позже"
                    }

                    else -> {
                        "Не удалось войти в аккаунт"
                    }
                }
            }

            else -> {
                "Не удалось войти в аккаунт"
            }
        }
    }

    private fun getRegisterErrorMessage(
        exception: Exception
    ): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                "Пароль слишком слабый. Используйте минимум 6 символов"
            }

            is FirebaseAuthInvalidCredentialsException -> {
                "Введите корректный email"
            }

            is FirebaseAuthUserCollisionException -> {
                "Пользователь с таким email уже зарегистрирован"
            }

            is FirebaseNetworkException -> {
                "Проблема с подключением к интернету"
            }

            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> {
                        "Введите корректный email"
                    }

                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        "Пользователь с таким email уже зарегистрирован"
                    }

                    "ERROR_WEAK_PASSWORD" -> {
                        "Пароль слишком слабый. Используйте минимум 6 символов"
                    }

                    "ERROR_TOO_MANY_REQUESTS" -> {
                        "Слишком много попыток. Попробуйте позже"
                    }

                    else -> {
                        "Не удалось создать аккаунт"
                    }
                }
            }

            else -> {
                "Не удалось создать аккаунт"
            }
        }
    }
}