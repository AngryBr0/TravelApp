package com.example.travelapp.presentation.profile

/**
 * ProfileUiState — состояние экрана профиля.
 *
 * Здесь хранятся:
 * - данные текущего пользователя;
 * - состояние загрузки;
 * - ошибка;
 * - признак выхода из аккаунта.
 */
data class ProfileUiState(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedOut: Boolean = false
)