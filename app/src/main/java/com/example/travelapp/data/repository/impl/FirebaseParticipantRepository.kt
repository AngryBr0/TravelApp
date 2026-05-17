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
import kotlinx.coroutines.tasks.await

/**
 * FirebaseParticipantRepository — реальная реализация ParticipantRepository через Firestore.
 *
 * Этот класс отвечает за участников конкретной поездки.
 *
 * Структура хранения:
 *
 * trips/
 *   tripId/
 *     participants/
 *       participantId/
 *         id
 *         tripId
 *         email
 *         role
 *         status
 *
 * ViewModel не знает, что используется Firestore.
 * Она работает только через интерфейс ParticipantRepository.
 */
class FirebaseParticipantRepository(
    private val firestore: FirebaseFirestore
) : ParticipantRepository {

    /**
     * Возвращает ссылку на подколлекцию participants конкретной поездки.
     */
    private fun participantsCollection(tripId: String) =
        firestore
            .collection("trips")
            .document(tripId)
            .collection("participants")

    /**
     * Приглашает участника в поездку.
     *
     * Сейчас приглашение реализовано упрощенно:
     * пользователь вводит email, а приложение сохраняет запись участника
     * со статусом INVITED.
     *
     * Это достаточно для прототипа ВКР.
     */
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

            /**
             * Проверяем, не был ли такой email уже добавлен в участники.
             * Это простая защита от дублей.
             */
            val existingParticipantSnapshot = participantsCollection(tripId)
                .whereEqualTo("email", participant.email)
                .get()
                .await()

            if (!existingParticipantSnapshot.isEmpty) {
                return AppResult.Error("Этот участник уже добавлен")
            }

            val normalizedEmail = participant.email.trim().lowercase()

            val document = participantsCollection(tripId)
                .document(normalizedEmail)

            val participantWithId = participant.copy(
                id = normalizedEmail,
                tripId = tripId,
                email = normalizedEmail
            )

            /**
             * enum role и status сохраняем как строки.
             * Так их проще восстановить при чтении из Firestore.
             */
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

    /**
     * Подписывается на список участников поездки.
     *
     * addSnapshotListener позволяет автоматически обновлять экран,
     * когда в Firestore появляется новый участник.
     */
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

                val participants = snapshot.documents
                    .mapNotNull { document ->
                        document.toTripParticipantOrNull()
                    }

                trySend(AppResult.Success(participants))
            }

        /**
         * Когда экран больше не слушает Flow,
         * удаляем Firebase listener.
         */
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Преобразует документ Firestore в объект TripParticipant.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toTripParticipantOrNull(): TripParticipant? {
        val id = getString("id") ?: this.id
        val email = getString("email") ?: return null

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
            name = getString("name").orEmpty(),
            role = role,
            status = status
        )
    }
}