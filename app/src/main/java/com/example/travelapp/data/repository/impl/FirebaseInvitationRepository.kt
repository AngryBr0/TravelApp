package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.InvitationStatus
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.ParticipantStatus
import com.example.travelapp.data.model.TripInvitation
import com.example.travelapp.data.repository.InvitationRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseInvitationRepository — реальная реализация приглашений через Firestore.
 *
 * Главная идея:
 * - приглашения хранятся в отдельной коллекции invitations;
 * - пользователь видит приглашения по своему email;
 * - после принятия его uid добавляется в trips/{tripId}.participants.
 */
class FirebaseInvitationRepository(
    private val firestore: FirebaseFirestore
) : InvitationRepository {

    private val invitationsCollection =
        firestore.collection("invitations")

    /**
     * Создает приглашение.
     */
    override suspend fun createInvitation(
        invitation: TripInvitation
    ): AppResult<Unit> {
        return try {
            if (invitation.inviteeEmail.isBlank()) {
                return AppResult.Error("Введите email участника")
            }

            val document = invitationsCollection.document()

            val invitationWithId = invitation.copy(
                id = document.id,
                inviteeEmail = invitation.inviteeEmail.trim().lowercase()
            )

            val invitationMap = mapOf(
                "id" to invitationWithId.id,
                "tripId" to invitationWithId.tripId,
                "tripTitle" to invitationWithId.tripTitle,
                "inviterUserId" to invitationWithId.inviterUserId,
                "inviteeEmail" to invitationWithId.inviteeEmail,
                "role" to invitationWithId.role.name,
                "status" to invitationWithId.status.name,
                "createdAt" to invitationWithId.createdAt
            )

            document.set(invitationMap).await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка создания приглашения"
            )
        }
    }

    /**
     * Загружает входящие приглашения пользователя по email.
     *
     * Второй пользователь входит в приложение,
     * мы берем его email из FirebaseAuth и ищем приглашения
     * со статусом PENDING.
     */
    override fun observePendingInvitations(
        email: String
    ): Flow<AppResult<List<TripInvitation>>> = callbackFlow {
        trySend(AppResult.Loading)

        val normalizedEmail = email.trim().lowercase()

        val listener = invitationsCollection
            .whereEqualTo("inviteeEmail", normalizedEmail)
            .whereEqualTo("status", InvitationStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки приглашений"
                        )
                    )
                    return@addSnapshotListener
                }

                val invitations = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.toInvitationOrNull()
                    }
                    ?: emptyList()

                trySend(AppResult.Success(invitations))
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Принимает приглашение.
     *
     * Здесь происходит главное:
     * 1. userId добавляется в массив participants у поездки;
     * 2. пользователь добавляется в подколлекцию participants;
     * 3. приглашение получает статус ACCEPTED.
     *
     * После пункта 1 поездка появится у пользователя,
     * потому что TripRepository ищет поездки через participants.
     */
    override suspend fun acceptInvitation(
        invitation: TripInvitation,
        userId: String
    ): AppResult<Unit> {
        return try {
            val tripDocument = firestore
                .collection("trips")
                .document(invitation.tripId)

            val participantDocument = tripDocument
                .collection("participants")
                .document(invitation.inviteeEmail)

            val invitationDocument = invitationsCollection
                .document(invitation.id)

            val participantMap = mapOf(
                "id" to userId,
                "tripId" to invitation.tripId,
                "email" to invitation.inviteeEmail,
                "role" to invitation.role.name,
                "status" to ParticipantStatus.ACCEPTED.name
            )

            /**
             * batch нужен, чтобы выполнить несколько операций одним набором:
             * обновить поездку, участника и приглашение.
             */
            val batch = firestore.batch()

            batch.update(
                tripDocument,
                "participants",
                FieldValue.arrayUnion(userId)
            )

            batch.set(
                participantDocument,
                participantMap
            )

            batch.update(
                invitationDocument,
                "status",
                InvitationStatus.ACCEPTED.name
            )

            batch.commit().await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка принятия приглашения"
            )
        }
    }

    /**
     * Отклоняет приглашение.
     */
    override suspend fun declineInvitation(
        invitationId: String
    ): AppResult<Unit> {
        return try {
            invitationsCollection
                .document(invitationId)
                .update("status", InvitationStatus.DECLINED.name)
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка отклонения приглашения"
            )
        }
    }

    /**
     * Преобразует документ Firestore в TripInvitation.
     */
    private fun DocumentSnapshot.toInvitationOrNull(): TripInvitation? {
        val id = getString("id") ?: this.id
        val tripId = getString("tripId") ?: return null
        val inviteeEmail = getString("inviteeEmail") ?: return null

        val roleText = getString("role") ?: ParticipantRole.VIEWER.name
        val statusText = getString("status") ?: InvitationStatus.PENDING.name

        val role = runCatching {
            ParticipantRole.valueOf(roleText)
        }.getOrDefault(ParticipantRole.VIEWER)

        val status = runCatching {
            InvitationStatus.valueOf(statusText)
        }.getOrDefault(InvitationStatus.PENDING)

        return TripInvitation(
            id = id,
            tripId = tripId,
            tripTitle = getString("tripTitle").orEmpty(),
            inviterUserId = getString("inviterUserId").orEmpty(),
            inviteeEmail = inviteeEmail,
            role = role,
            status = status,
            createdAt = getString("createdAt").orEmpty()
        )
    }
}