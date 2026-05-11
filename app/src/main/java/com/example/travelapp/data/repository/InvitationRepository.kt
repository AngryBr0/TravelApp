package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.TripInvitation
import kotlinx.coroutines.flow.Flow

/**
 * InvitationRepository — интерфейс для работы с приглашениями.
 *
 * Он отделяет ViewModel от конкретной реализации Firestore.
 */
interface InvitationRepository {

    /**
     * Создать приглашение в поездку.
     */
    suspend fun createInvitation(
        invitation: TripInvitation
    ): AppResult<Unit>

    /**
     * Получить входящие приглашения текущего пользователя по email.
     */
    fun observePendingInvitations(
        email: String
    ): Flow<AppResult<List<TripInvitation>>>

    /**
     * Принять приглашение.
     *
     * После принятия userId добавляется в participants поездки,
     * и поездка начинает отображаться у приглашенного пользователя.
     */
    suspend fun acceptInvitation(
        invitation: TripInvitation,
        userId: String
    ): AppResult<Unit>

    /**
     * Отклонить приглашение.
     */
    suspend fun declineInvitation(
        invitationId: String
    ): AppResult<Unit>
}