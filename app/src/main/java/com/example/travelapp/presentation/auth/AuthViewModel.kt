package com.example.travelapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel управляет состоянием экранов входа и регистрации.
 *
 * Экран не должен напрямую обращаться в AuthRepository.
 * Экран только вызывает методы ViewModel:
 *
 * updateEmail()
 * updatePassword()
 * signIn()
 * signUp()
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Внутреннее состояние экрана.
     * private — чтобы его нельзя было изменить напрямую из UI.
     */
    private val _uiState = MutableStateFlow(AuthUiState())

    /**
     * Публичное состояние.
     * Экран может только читать это состояние.
     */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    /**
     * Вход пользователя.
     *
     * viewModelScope.launch запускает корутину.
     * Это нужно, потому что вход может быть долгой операцией.
     */
    fun signIn() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите email"
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректный email"
            )
            return
        }

        if (state.password.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите пароль"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.signIn(
                email = state.email.trim(),
                password = state.password
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthorized = true
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }
    /**
     * Регистрация пользователя.
     */
    fun signUp() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите имя"
            )
            return
        }

        if (state.email.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите email"
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректный email"
            )
            return
        }

        if (state.password.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите пароль"
            )
            return
        }

        if (state.password.length < 6) {
            _uiState.value = state.copy(
                errorMessage = "Пароль должен содержать минимум 6 символов"
            )
            return
        }

        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(
                errorMessage = "Пароли не совпадают"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.signUp(
                email = state.email.trim(),
                password = state.password,
                name = state.name.trim()
            )

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthorized = true
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }
}