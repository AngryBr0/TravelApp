package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripParticipant
import kotlinx.coroutines.flow.Flow

/**
 * ParticipantRepository — интерфейс для работы с участниками поездки.
 */
interface ParticipantRepository {
    suspend fun inviteParticipant(
        tripId: String,
        participant: TripParticipant
    ): AppResult<Unit>
    fun observeParticipants(
        tripId: String
    ): Flow<AppResult<List<TripParticipant>>>
    suspend fun updateParticipantRole(
        tripId: String,
        participantId: String,
        role: ParticipantRole
    ): AppResult<Unit>
    suspend fun deleteParticipant(
        tripId: String,
        participantId: String,
        participantEmail: String
    ): AppResult<Unit>
}