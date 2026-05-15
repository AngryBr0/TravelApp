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
            name = user.name,
            editableName = user.name,
            editableEmail = user.email
        )
    }
    fun startEditing() {
        val state = _uiState.value

        _uiState.value = state.copy(
            isEditing = true,
            editableName = state.name,
            editableEmail = state.email,
            errorMessage = null
        )
    }

    fun cancelEditing() {
        val state = _uiState.value

        _uiState.value = state.copy(
            isEditing = false,
            editableName = state.name,
            editableEmail = state.email,
            errorMessage = null
        )
    }

    fun updateEditableName(name: String) {
        _uiState.value = _uiState.value.copy(
            editableName = name
        )
    }

    fun updateEditableEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            editableEmail = email
        )
    }

    /**
     * Сохраняет изменения профиля.
     */
    fun saveProfile() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = state.copy(
                isSaving = true,
                errorMessage = null
            )

            when (
                val result = authRepository.updateProfile(
                    name = state.editableName,
                    email = state.editableEmail
                )
            ) {
                is AppResult.Success -> {
                    val user = result.data

                    _uiState.value = _uiState.value.copy(
                        name = user.name,
                        email = user.email,
                        editableName = user.name,
                        editableEmail = user.email,
                        isEditing = false,
                        isSaving = false,
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
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