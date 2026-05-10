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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.signIn(
                email = _uiState.value.email,
                password = _uiState.value.password
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.signUp(
                email = _uiState.value.email,
                password = _uiState.value.password,
                name = _uiState.value.name
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