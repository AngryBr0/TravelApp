package com.example.travelapp.presentation.participants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.data.repository.ParticipantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ParticipantsViewModel отвечает за вкладку участников поездки.
 *
 * Она:
 * - хранит состояние вкладки;
 * - получает список участников из репозитория;
 * - проверяет email;
 * - добавляет нового участника;
 * - преобразует текстовую роль в enum ParticipantRole.
 */
class ParticipantsViewModel(
    private val participantRepository: ParticipantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParticipantsUiState())
    val uiState: StateFlow<ParticipantsUiState> = _uiState.asStateFlow()

    /**
     * Job нужен, чтобы не запускать несколько подписок
     * на участников одной и той же поездки.
     */
    private var observeJob: Job? = null

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateRole(role: String) {
        _uiState.value = _uiState.value.copy(role = role)
    }

    /**
     * Загружает участников выбранной поездки.
     */
    fun loadParticipants(tripId: String) {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            participantRepository.observeParticipants(tripId).collect { result ->
                when (result) {
                    AppResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            participants = result.data,
                            errorMessage = null
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
     * Добавляет нового участника в поездку.
     */
    fun inviteParticipant(tripId: String) {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите email участника"
            )
            return
        }

        if (!state.email.contains("@")) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректный email"
            )
            return
        }

        val participant = TripParticipant(
            tripId = tripId,
            email = state.email,
            role = parseRole(state.role)
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = participantRepository.inviteParticipant(
                    tripId = tripId,
                    participant = participant
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        email = "",
                        role = "viewer",
                        errorMessage = null
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
     * Преобразует текстовую роль в enum ParticipantRole.
     *
     * Пользователь может ввести роль по-русски или по-английски,
     * а внутри приложения мы храним ее как enum.
     */
    private fun parseRole(role: String): ParticipantRole {
        return when (role.trim().lowercase()) {
            "organizer", "организатор" -> ParticipantRole.ORGANIZER
            "editor", "редактор" -> ParticipantRole.EDITOR
            "viewer", "наблюдатель" -> ParticipantRole.VIEWER
            else -> ParticipantRole.VIEWER
        }
    }
}