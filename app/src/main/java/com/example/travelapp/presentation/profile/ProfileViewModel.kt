package com.example.travelapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileViewModel отвечает за экран профиля.
 *
 * Она:
 * - получает данные текущего пользователя;
 * - хранит состояние экрана;
 * - выполняет выход из аккаунта.
 */
class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Загружает данные текущего пользователя.
     */
    fun loadProfile() {
        val user = authRepository.getCurrentUser()

        if (user == null) {
            _uiState.value = ProfileUiState(
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        _uiState.value = ProfileUiState(
            userId = user.id,
            email = user.email,
            name = user.name
        )
    }

    /**
     * Выполняет выход из аккаунта.
     *
     * signOut является suspend-функцией, поэтому вызывается внутри корутины.
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = authRepository.signOut()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignedOut = true
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