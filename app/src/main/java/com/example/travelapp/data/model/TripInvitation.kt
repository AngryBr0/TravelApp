package com.example.travelapp.data.model

/**
 * TripInvitation — приглашение пользователя в поездку.
 *
 * Этот класс нужен для настоящего сценария:
 * организатор приглашает по email, а другой пользователь
 * видит приглашение после входа в приложение со своего телефона.
 */
data class TripInvitation(
    val id: String = "",
    val tripId: String = "",
    val tripTitle: String = "",
    val inviterUserId: String = "",
    val inviteeEmail: String = "",
    val role: ParticipantRole = ParticipantRole.VIEWER,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: String = ""
)