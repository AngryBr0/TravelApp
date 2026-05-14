package com.example.travelapp.presentation.participants

import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripParticipant

/**
 * ParticipantsUiState — состояние вкладки участников.
 *
 * Здесь хранятся:
 * - список участников;
 * - email, который пользователь вводит в форме;
 * - роль нового участника;
 * - роль текущего пользователя в этой поездке;
 * - права текущего пользователя;
 * - состояние загрузки;
 * - текст ошибки.
 */
data class ParticipantsUiState(
    val isLoading: Boolean = false,

    val participants: List<TripParticipant> = emptyList(),

    val email: String = "",
    val role: String = "viewer",

    /**
     * Роль текущего пользователя в выбранной поездке.
     *
     * По умолчанию VIEWER, чтобы пользователь без найденной роли
     * не мог редактировать данные поездки.
     */
    val currentUserRole: ParticipantRole = ParticipantRole.VIEWER,

    /**
     * Может ли текущий пользователь редактировать маршрут и бюджет.
     *
     * ORGANIZER и EDITOR могут редактировать.
     * VIEWER только просматривает.
     */
    val canEditTrip: Boolean = false,

    /**
     * Может ли текущий пользователь приглашать других участников.
     *
     * В нашей логике это разрешено только ORGANIZER.
     */
    val canInviteParticipants: Boolean = false,

    val errorMessage: String? = null
)