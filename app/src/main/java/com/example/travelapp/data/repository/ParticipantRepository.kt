package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.TripParticipant
import kotlinx.coroutines.flow.Flow

interface ParticipantRepository {

    suspend fun inviteParticipant(
        tripId: String,
        participant: TripParticipant
    ): AppResult<Unit>

    fun observeParticipants(tripId: String): Flow<AppResult<List<TripParticipant>>>
}