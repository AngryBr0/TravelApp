package com.example.travelapp.presentation.participants

import com.example.travelapp.data.model.TripParticipant

/**
 * ParticipantsUiState — состояние вкладки участников.
 *
 * Здесь хранятся:
 * - список участников;
 * - email, который пользователь вводит в форме;
 * - роль нового участника;
 * - состояние загрузки;
 * - текст ошибки.
 */
data class ParticipantsUiState(
    val isLoading: Boolean = false,

    val participants: List<TripParticipant> = emptyList(),

    val email: String = "",
    val role: String = "viewer",

    val errorMessage: String? = null
)