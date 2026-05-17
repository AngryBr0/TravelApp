package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.ParticipantStatus
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.data.repository.ParticipantRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue
/**
 * FirebaseParticipantRepository — реальная реализация ParticipantRepository через Firestore.
 *
 * Репозиторий:
 * - читает участников поездки;
 * - добавляет приглашённых участников;
 * - подтягивает имена пользователей из users/{userId};
 * - добавляет организатора в список, если в старой поездке он не был записан.
 */
class FirebaseParticipantRepository(
    private val firestore: FirebaseFirestore
) : ParticipantRepository {

    private fun participantsCollection(tripId: String) =
        firestore
            .collection("trips")
            .document(tripId)
            .collection("participants")

    override suspend fun inviteParticipant(
        tripId: String,
        participant: TripParticipant
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (participant.email.isBlank()) {
                return AppResult.Error("Введите email участника")
            }

            if (!participant.email.contains("@")) {
                return AppResult.Error("Введите корректный email")
            }

            val normalizedEmail = participant.email.trim().lowercase()

            val existingParticipantSnapshot = participantsCollection(tripId)
                .whereEqualTo("email", normalizedEmail)
                .get()
                .await()

            if (!existingParticipantSnapshot.isEmpty) {
                return AppResult.Error("Этот участник уже добавлен")
            }

            val document = participantsCollection(tripId)
                .document(normalizedEmail)

            val participantWithId = participant.copy(
                id = normalizedEmail,
                tripId = tripId,
                email = normalizedEmail,
                name = participant.name
            )

            val participantMap = mapOf(
                "id" to participantWithId.id,
                "tripId" to participantWithId.tripId,
                "email" to participantWithId.email,
                "name" to participantWithId.name,
                "role" to participantWithId.role.name,
                "status" to participantWithId.status.name
            )

            document.set(participantMap).await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка приглашения участника"
            )
        }
    }

    override fun observeParticipants(
        tripId: String
    ): Flow<AppResult<List<TripParticipant>>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = participantsCollection(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки участников"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(AppResult.Success(emptyList()))
                    return@addSnapshotListener
                }

                launch {
                    val rawParticipants = snapshot.documents
                        .mapNotNull { document ->
                            document.toTripParticipantOrNull()
                        }

                    val participantsWithOrganizer = ensureOrganizerExists(
                        tripId = tripId,
                        participants = rawParticipants
                    )

                    val enrichedParticipants = participantsWithOrganizer
                        .map { participant ->
                            enrichParticipantWithUserData(participant)
                        }
                        .sortedWith(
                            compareByDescending<TripParticipant> { participant ->
                                participant.role == ParticipantRole.ORGANIZER
                            }.thenBy { participant ->
                                participant.name.ifBlank { participant.email }
                            }
                        )

                    trySend(AppResult.Success(enrichedParticipants))
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Добавляет организатора в список, если это старая поездка,
     * где организатор ещё не был записан в participants.
     */
    private suspend fun ensureOrganizerExists(
        tripId: String,
        participants: List<TripParticipant>
    ): List<TripParticipant> {
        val tripDocument = firestore
            .collection("trips")
            .document(tripId)
            .get()
            .await()

        val ownerId = tripDocument
            .getString("ownerId")
            .orEmpty()

        if (ownerId.isBlank()) {
            return participants
        }

        val organizerAlreadyExists = participants.any { participant ->
            participant.id == ownerId || participant.role == ParticipantRole.ORGANIZER
        }

        if (organizerAlreadyExists) {
            return participants
        }

        val ownerUserDocument = firestore
            .collection("users")
            .document(ownerId)
            .get()
            .await()

        val ownerEmail = ownerUserDocument
            .getString("email")
            .orEmpty()

        val ownerName = ownerUserDocument
            .getString("name")
            .orEmpty()
            .ifBlank {
                ownerEmail.substringBefore("@")
            }
            .ifBlank {
                "Организатор"
            }

        val organizer = TripParticipant(
            id = ownerId,
            tripId = tripId,
            email = ownerEmail,
            name = ownerName,
            role = ParticipantRole.ORGANIZER,
            status = ParticipantStatus.ACCEPTED
        )

        /**
         * Сохраняем организатора в Firestore,
         * чтобы в следующий раз он уже был нормальным участником поездки.
         */
        participantsCollection(tripId)
            .document(ownerId)
            .set(
                mapOf(
                    "id" to organizer.id,
                    "tripId" to organizer.tripId,
                    "email" to organizer.email,
                    "name" to organizer.name,
                    "role" to organizer.role.name,
                    "status" to organizer.status.name
                )
            )
            .await()

        return listOf(organizer) + participants
    }

    /**
     * Дополняет участника email/name из users/{id}.
     *
     * Это чинит старые документы, где у организатора email был пустой,
     * а name вообще не сохранялся.
     */
    private suspend fun enrichParticipantWithUserData(
        participant: TripParticipant
    ): TripParticipant {
        val userDocument = firestore
            .collection("users")
            .document(participant.id)
            .get()
            .await()

        val userEmail = userDocument
            .getString("email")
            .orEmpty()

        val finalEmail = participant.email
            .ifBlank { userEmail }

        val userName = userDocument
            .getString("name")
            .orEmpty()

        val finalName = participant.name
            .ifBlank { userName }
            .ifBlank { finalEmail.substringBefore("@") }
            .ifBlank {
                if (participant.role == ParticipantRole.ORGANIZER) {
                    "Организатор"
                } else {
                    "Участник"
                }
            }

        return participant.copy(
            email = finalEmail,
            name = finalName
        )
    }

    /**
     * Обновляет роль участника поездки.
     *
     * Важно:
     * у приглашённых пользователей documentId может быть email,
     * а после принятия приглашения поле id становится userId.
     * Поэтому сначала ищем документ по полю "id".
     */
    override suspend fun updateParticipantRole(
        tripId: String,
        participantId: String,
        role: ParticipantRole
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (participantId.isBlank()) {
                return AppResult.Error("Участник не найден")
            }

            if (role == ParticipantRole.ORGANIZER) {
                return AppResult.Error("Нельзя назначить организатора")
            }

            val participantSnapshot = participantsCollection(tripId)
                .whereEqualTo("id", participantId)
                .get()
                .await()

            val participantDocument = participantSnapshot.documents.firstOrNull()

            if (participantDocument != null) {
                participantDocument.reference
                    .update("role", role.name)
                    .await()
            } else {
                participantsCollection(tripId)
                    .document(participantId)
                    .update("role", role.name)
                    .await()
            }

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка изменения роли участника"
            )
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toTripParticipantOrNull(): TripParticipant? {
        val id = getString("id") ?: this.id

        val email = getString("email").orEmpty()
        val name = getString("name").orEmpty()

        val roleText = getString("role") ?: ParticipantRole.VIEWER.name
        val statusText = getString("status") ?: ParticipantStatus.INVITED.name

        val role = runCatching {
            ParticipantRole.valueOf(roleText)
        }.getOrDefault(ParticipantRole.VIEWER)

        val status = runCatching {
            ParticipantStatus.valueOf(statusText)
        }.getOrDefault(ParticipantStatus.INVITED)

        return TripParticipant(
            id = id,
            tripId = getString("tripId").orEmpty(),
            email = email,
            name = name,
            role = role,
            status = status
        )
    }
    /**
     * Удаляет участника из поездки.
     *
     * Работает для:
     * - ACCEPTED участника;
     * - INVITED участника;
     * - DECLINED участника.
     *
     * Также удаляет связанные приглашения, чтобы запись больше не висела.
     */
    override suspend fun deleteParticipant(
        tripId: String,
        participantId: String,
        participantEmail: String
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (participantId.isBlank() && participantEmail.isBlank()) {
                return AppResult.Error("Участник не найден")
            }

            val tripDocument = firestore
                .collection("trips")
                .document(tripId)

            val normalizedEmail = participantEmail
                .trim()
                .lowercase()

            /**
             * У принятого приглашения documentId может быть email,
             * а поле id уже содержит uid пользователя.
             *
             * Поэтому сначала ищем документ по полю id.
             */
            val participantByIdSnapshot = participantsCollection(tripId)
                .whereEqualTo("id", participantId)
                .get()
                .await()

            val participantDocument = participantByIdSnapshot.documents.firstOrNull()
                ?: if (normalizedEmail.isNotBlank()) {
                    participantsCollection(tripId)
                        .document(normalizedEmail)
                        .get()
                        .await()
                } else {
                    participantsCollection(tripId)
                        .document(participantId)
                        .get()
                        .await()
                }

            if (!participantDocument.exists()) {
                return AppResult.Error("Участник не найден")
            }

            val roleText = participantDocument.getString("role").orEmpty()

            if (roleText == ParticipantRole.ORGANIZER.name) {
                return AppResult.Error("Нельзя удалить организатора поездки")
            }

            val batch = firestore.batch()

            /**
             * Удаляем документ участника из trips/{tripId}/participants.
             */
            batch.delete(participantDocument.reference)

            /**
             * Если участник уже принимал приглашение,
             * его uid есть в массиве trips/{tripId}.participants.
             * Удаляем его оттуда, чтобы поездка пропала у пользователя.
             */
            if (participantId.isNotBlank()) {
                batch.update(
                    tripDocument,
                    "participants",
                    FieldValue.arrayRemove(participantId)
                )
            }

            /**
             * Удаляем связанные приглашения.
             *
             * Чтобы не ловить проблемы с составными индексами Firestore,
             * ищем приглашения по tripId, а email фильтруем уже в коде.
             */
            val invitationDocuments = firestore
                .collection("invitations")
                .whereEqualTo("tripId", tripId)
                .get()
                .await()
                .documents
                .filter { invitationDocument ->
                    val inviteeEmail = invitationDocument
                        .getString("inviteeEmail")
                        .orEmpty()
                        .trim()
                        .lowercase()

                    inviteeEmail == normalizedEmail
                }

            invitationDocuments.forEach { invitationDocument ->
                batch.delete(invitationDocument.reference)
            }

            batch.commit().await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка удаления участника"
            )
        }
    }
}
