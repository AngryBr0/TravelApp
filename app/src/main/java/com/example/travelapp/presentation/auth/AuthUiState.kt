package com.example.travelapp.presentation.auth

/**
 * AuthUiState — состояние экранов входа и регистрации.
 *
 * Здесь хранятся значения полей формы и состояние авторизации.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthorized: Boolean = false
)