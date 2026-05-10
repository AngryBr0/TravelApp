package com.example.travelapp.presentation.notifications


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * NotificationsViewModel отвечает за экран уведомлений.
 *
 * Она:
 * - получает id текущего пользователя;
 * - подписывается на его уведомления;
 * - обновляет состояние экрана.
 */
class NotificationsViewModel(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    /**
     * Загружает уведомления текущего пользователя.
     */
    fun loadNotifications() {
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = NotificationsUiState(
                isLoading = false,
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            notificationRepository.observeNotifications(userId).collect { result ->
                when (result) {
                    AppResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is AppResult.Success -> {
                        _uiState.value = NotificationsUiState(
                            isLoading = false,
                            notifications = result.data
                        )
                    }

                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}