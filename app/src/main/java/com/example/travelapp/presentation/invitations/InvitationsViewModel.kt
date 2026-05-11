package com.example.travelapp.presentation.invitations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.TripInvitation
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.InvitationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * InvitationsViewModel отвечает за экран входящих приглашений.
 *
 * Логика:
 * - берём email текущего пользователя;
 * - ищем приглашения по этому email;
 * - пользователь может принять или отклонить приглашение.
 */
class InvitationsViewModel(
    private val authRepository: AuthRepository,
    private val invitationRepository: InvitationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationsUiState())
    val uiState: StateFlow<InvitationsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    /**
     * Загружает входящие приглашения текущего пользователя.
     */
    fun loadInvitations() {
        val user = authRepository.getCurrentUser()

        if (user == null) {
            _uiState.value = InvitationsUiState(
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        if (user.email.isBlank()) {
            _uiState.value = InvitationsUiState(
                errorMessage = "У пользователя отсутствует email"
            )
            return
        }

        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            invitationRepository.observePendingInvitations(user.email)
                .collect { result ->
                    when (result) {
                        AppResult.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                errorMessage = null
                            )
                        }

                        is AppResult.Success -> {
                            _uiState.value = InvitationsUiState(
                                isLoading = false,
                                invitations = result.data
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

    /**
     * Принимает приглашение.
     *
     * После принятия userId добавляется в participants поездки,
     * и поездка появляется у пользователя в списке поездок.
     */
    fun acceptInvitation(invitation: TripInvitation) {
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        viewModelScope.launch {
            when (
                val result = invitationRepository.acceptInvitation(
                    invitation = invitation,
                    userId = userId
                )
            ) {
                is AppResult.Success -> {
                    // Ничего вручную удалять не надо:
                    // после смены статуса на ACCEPTED приглашение исчезнет из списка,
                    // потому что экран показывает только PENDING.
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Отклоняет приглашение.
     */
    fun declineInvitation(invitation: TripInvitation) {
        viewModelScope.launch {
            when (
                val result = invitationRepository.declineInvitation(
                    invitationId = invitation.id
                )
            ) {
                is AppResult.Success -> Unit

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }
}