package com.example.travelapp.data.model

data class TripParticipant(
    val id: String = "",
    val tripId: String = "",
    val email: String = "",
    val role: ParticipantRole = ParticipantRole.VIEWER,
    val status: ParticipantStatus = ParticipantStatus.INVITED
)