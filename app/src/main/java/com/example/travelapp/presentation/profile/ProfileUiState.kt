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

    val editableName: String = "",
    val editableEmail: String = "",

    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,

    val errorMessage: String? = null,
    val isSignedOut: Boolean = false
)