package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripParticipant
import kotlinx.coroutines.flow.Flow

/**
 * ParticipantRepository — интерфейс для работы с участниками поездки.
 */
interface ParticipantRepository {

    /**
     * Пригласить участника в поездку.
     */
    suspend fun inviteParticipant(
        tripId: String,
        participant: TripParticipant
    ): AppResult<Unit>

    /**
     * Наблюдать за списком участников поездки.
     */
    fun observeParticipants(
        tripId: String
    ): Flow<AppResult<List<TripParticipant>>>

    /**
     * Изменить роль участника.
     */
    suspend fun updateParticipantRole(
        tripId: String,
        participantId: String,
        role: ParticipantRole
    ): AppResult<Unit>

    /**
     * Удалить участника из поездки.
     *
     * participantId нужен для принятого участника,
     * participantEmail нужен для приглашённого/ожидающего участника.
     */
    suspend fun deleteParticipant(
        tripId: String,
        participantId: String,
        participantEmail: String
    ): AppResult<Unit>
}