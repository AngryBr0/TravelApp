package com.example.travelapp.presentation.participants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripInvitation
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.InvitationRepository
import com.example.travelapp.data.repository.NotificationRepository
import com.example.travelapp.data.repository.ParticipantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ParticipantsViewModel отвечает за вкладку участников поездки.
 *
 * Теперь она делает не просто добавление участника,
 * а настоящее приглашение:
 *
 * 1. Создает запись в invitations.
 * 2. Добавляет участника в подколлекцию participants со статусом INVITED.
 * 3. Создает уведомление для организатора.
 *
 * Второй пользователь увидит приглашение на экране "Приглашения"
 * после входа под своим email.
 */
class ParticipantsViewModel(
    private val participantRepository: ParticipantRepository,
    private val invitationRepository: InvitationRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParticipantsUiState())
    val uiState: StateFlow<ParticipantsUiState> = _uiState.asStateFlow()

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
                        val participants = result.data
                        val currentUser = authRepository.getCurrentUser()

                        /**
                         * Находим роль текущего пользователя в этой поездке.
                         *
                         * Для организатора проверяем id.
                         * Для приглашенного пользователя проверяем и id, и email,
                         * потому что до принятия приглашения запись может быть связана с email,
                         * а после принятия — с userId.
                         */
                        val normalizedEmail = currentUser?.email.orEmpty().trim().lowercase()

                        val currentParticipant = participants.firstOrNull { participant ->
                            participant.id == currentUser?.id ||
                                    participant.email.trim().lowercase() == normalizedEmail
                        }

                        val currentRole = currentParticipant?.role ?: ParticipantRole.VIEWER

                        val canEditTrip =
                            currentRole == ParticipantRole.ORGANIZER ||
                                    currentRole == ParticipantRole.EDITOR

                        val canInviteParticipants =
                            currentRole == ParticipantRole.ORGANIZER

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            participants = participants,
                            currentUserRole = currentRole,
                            canEditTrip = canEditTrip,
                            canInviteParticipants = canInviteParticipants,
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
     * Создает настоящее приглашение в поездку.
     *
     * tripTitle нужен, чтобы второй пользователь видел,
     * в какую поездку его пригласили.
     */
    fun inviteParticipant(
        tripId: String,
        tripTitle: String
    ) {
        val state = _uiState.value
        val normalizedEmail = state.email.trim().lowercase()

        if (normalizedEmail.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите email участника"
            )
            return
        }

        if (!normalizedEmail.contains("@")) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректный email"
            )
            return
        }

        val currentUser = authRepository.getCurrentUser()

        if (currentUser == null) {
            _uiState.value = state.copy(
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        val role = parseRole(state.role)

        val invitation = TripInvitation(
            tripId = tripId,
            tripTitle = tripTitle.ifBlank { "Поездка" },
            inviterUserId = currentUser.id,
            inviteeEmail = normalizedEmail,
            role = role,
            createdAt = getCurrentDateTime()
        )

        val participant = TripParticipant(
            tripId = tripId,
            email = normalizedEmail,
            role = role
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            val invitationResult = invitationRepository.createInvitation(invitation)

            if (invitationResult is AppResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = invitationResult.message
                )
                return@launch
            }

            val participantResult = participantRepository.inviteParticipant(
                tripId = tripId,
                participant = participant
            )

            when (participantResult) {
                is AppResult.Success -> {
                    notificationRepository.addNotification(
                        NotificationItem(
                            userId = currentUser.id,
                            tripId = tripId,
                            text = "Приглашён участник: $normalizedEmail",
                            createdAt = getCurrentDateTime()
                        )
                    )

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
                        errorMessage = participantResult.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }
    /**
     * Меняет роль участника.
     *
     * Доступно только организатору.
     */
    fun updateParticipantRole(
        tripId: String,
        participantId: String,
        role: ParticipantRole
    ) {
        val state = _uiState.value

        if (state.currentUserRole != ParticipantRole.ORGANIZER) {
            _uiState.value = state.copy(
                errorMessage = "Только организатор может менять роли"
            )
            return
        }

        val participant = state.participants.firstOrNull { item ->
            item.id == participantId
        }

        if (participant == null) {
            _uiState.value = state.copy(
                errorMessage = "Участник не найден"
            )
            return
        }

        if (participant.role == ParticipantRole.ORGANIZER) {
            _uiState.value = state.copy(
                errorMessage = "Нельзя изменить роль организатора"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = participantRepository.updateParticipantRole(
                    tripId = tripId,
                    participantId = participantId,
                    role = role
                )
            ) {
                is AppResult.Success -> {
                    val updatedParticipants = _uiState.value.participants.map { item ->
                        if (item.id == participantId) {
                            item.copy(role = role)
                        } else {
                            item
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        participants = updatedParticipants,
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
     * Удаляет участника из поездки.
     *
     * Доступно только организатору.
     */
    fun deleteParticipant(
        tripId: String,
        participantId: String,
        participantEmail: String
    ) {
        val state = _uiState.value

        if (state.currentUserRole != ParticipantRole.ORGANIZER) {
            _uiState.value = state.copy(
                errorMessage = "Только организатор может удалять участников"
            )
            return
        }

        val participant = state.participants.firstOrNull { item ->
            item.id == participantId
        }

        if (participant == null) {
            _uiState.value = state.copy(
                errorMessage = "Участник не найден"
            )
            return
        }

        if (participant.role == ParticipantRole.ORGANIZER) {
            _uiState.value = state.copy(
                errorMessage = "Нельзя удалить организатора"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = participantRepository.deleteParticipant(
                    tripId = tripId,
                    participantId = participantId,
                    participantEmail = participantEmail
                )
            ) {
                is AppResult.Success<*> -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        participants = _uiState.value.participants.filter { item ->
                            item.id != participantId
                        },
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
     * Преобразует текстовую роль в enum.
     */
    private fun parseRole(role: String): ParticipantRole {
        return when (role.trim().lowercase()) {
            "organizer", "организатор" -> ParticipantRole.ORGANIZER
            "editor", "редактор" -> ParticipantRole.EDITOR
            "viewer", "наблюдатель" -> ParticipantRole.VIEWER
            else -> ParticipantRole.VIEWER
        }
    }

    /**
     * Возвращает текущую дату и время.
     */
    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
    }


}