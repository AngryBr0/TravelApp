package com.example.travelapp.presentation.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthorized: Boolean = false
)