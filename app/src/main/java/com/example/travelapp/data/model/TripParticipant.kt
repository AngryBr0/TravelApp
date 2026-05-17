package com.example.travelapp.data.model

/**
 * TripParticipant — участник поездки.
 *
 * name нужен, чтобы в интерфейсе показывать имя участника,
 * а не только email.
 */
data class TripParticipant(
    val id: String = "",
    val tripId: String = "",
    val email: String = "",
    val name: String = "",
    val role: ParticipantRole = ParticipantRole.VIEWER,
    val status: ParticipantStatus = ParticipantStatus.INVITED
)