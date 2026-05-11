package com.example.travelapp.presentation.invitations

import com.example.travelapp.data.model.TripInvitation

/**
 * InvitationsUiState — состояние экрана входящих приглашений.
 *
 * Здесь хранятся:
 * - список приглашений;
 * - состояние загрузки;
 * - текст ошибки.
 */
data class InvitationsUiState(
    val isLoading: Boolean = false,
    val invitations: List<TripInvitation> = emptyList(),
    val errorMessage: String? = null
)