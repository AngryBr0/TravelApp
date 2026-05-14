package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.data.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.CollectionReference
/**
 * FirebaseTripRepository — реальная реализация TripRepository через Firestore.
 *
 * Этот класс заменяет FakeTripRepository.
 *
 * Он отвечает за:
 * - создание поездки;
 * - получение списка поездок пользователя;
 * - получение одной поездки по id;
 * - удаление поездки.
 *
 * ViewModel не знает, что данные хранятся в Firebase.
 * Она работает только с интерфейсом TripRepository.
 */
class FirebaseTripRepository(
    private val firestore: FirebaseFirestore
) : TripRepository {

    /**
     * Название коллекции поездок в Firestore.
     *
     * Структура:
     *
     * trips/
     *   tripId/
     *     title
     *     description
     *     startDate
     *     endDate
     *     status
     *     ownerId
     *     participants
     */
    private val tripsCollection = firestore.collection("trips")

    /**
     * Создаёт новую поездку в Firestore.
     *
     * document() без аргументов создаёт новый документ
     * с автоматически сгенерированным id.
     */
    override suspend fun createTrip(trip: Trip): AppResult<Unit> {
        return try {
            if (trip.title.isBlank()) {
                return AppResult.Error("Введите название поездки")
            }

            if (trip.ownerId.isBlank()) {
                return AppResult.Error("Пользователь не авторизован")
            }

            val document = tripsCollection.document()

            val tripWithId = trip.copy(
                id = document.id
            )

            /**
             * Firestore лучше сохранять простыми типами:
             * String, Number, Boolean, List, Map.
             *
             * Поэтому enum TripStatus сохраняем как строку через .name.
             */
            val tripMap = mapOf(
                "id" to tripWithId.id,
                "title" to tripWithId.title,
                "description" to tripWithId.description,
                "startDate" to tripWithId.startDate,
                "endDate" to tripWithId.endDate,
                "status" to tripWithId.status.name,
                "ownerId" to tripWithId.ownerId,
                "participants" to tripWithId.participants
            )

            document.set(tripMap).await()

            /**
             * При создании поездки сразу добавляем создателя
             * в подколлекцию participants как организатора.
             *
             * Это нужно, чтобы во вкладке "Участники" был виден
             * не только приглашенный пользователь, но и сам организатор.
             */
            val organizerParticipant = mapOf(
                "id" to tripWithId.ownerId,
                "tripId" to tripWithId.id,
                "email" to "",
                "role" to "ORGANIZER",
                "status" to "ACCEPTED"
            )

            document
                .collection("participants")
                .document(tripWithId.ownerId)
                .set(organizerParticipant)
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка создания поездки"
            )
        }
    }

    /**
     * Подписывается на поездки пользователя.
     *
     * whereArrayContains("participants", userId) означает:
     * получить только те поездки, где userId есть в списке participants.
     *
     * callbackFlow превращает Firebase Listener в Kotlin Flow.
     */
    override fun observeTrips(userId: String): Flow<AppResult<List<Trip>>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = tripsCollection
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки поездок"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(AppResult.Success(emptyList()))
                    return@addSnapshotListener
                }

                val trips = snapshot.documents.mapNotNull { document ->
                    document.toTripOrNull()
                }

                trySend(AppResult.Success(trips))
            }

        /**
         * awaitClose вызывается, когда Flow больше не используется.
         * Здесь мы удаляем Firebase listener, чтобы не было утечек памяти.
         */
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Подписывается на одну конкретную поездку по id.
     */
    override fun observeTripById(tripId: String): Flow<AppResult<Trip>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = tripsCollection
            .document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки поездки"
                        )
                    )
                    return@addSnapshotListener
                }

                val trip = snapshot?.toTripOrNull()

                if (trip == null) {
                    trySend(AppResult.Error("Поездка не найдена"))
                } else {
                    trySend(AppResult.Success(trip))
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Удаляет поездку из Firestore.
     *
     * Важно:
     * Firestore сам не удаляет подколлекции при удалении документа.
     * Поэтому для прототипа мы вручную удаляем основные подколлекции:
     * routePoints, expenses, participants.
     *
     * Также удаляем приглашения и уведомления, связанные с поездкой.
     */
    override suspend fun deleteTrip(tripId: String): AppResult<Unit> {
        return try {
            val tripDocument = tripsCollection.document(tripId)

            deleteCollection(
                tripDocument.collection("routePoints")
            )

            deleteCollection(
                tripDocument.collection("expenses")
            )

            deleteCollection(
                tripDocument.collection("participants")
            )

            deleteQueryResult(
                collectionName = "invitations",
                field = "tripId",
                value = tripId
            )

            deleteQueryResult(
                collectionName = "notifications",
                field = "tripId",
                value = tripId
            )

            tripDocument.delete().await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка удаления поездки"
            )
        }
    }

    /**
     * Преобразует документ Firestore в объект Trip.
     *
     * Мы не используем document.toObject(Trip::class.java),
     * потому что вручную безопаснее обработать enum TripStatus.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toTripOrNull(): Trip? {
        val id = getString("id") ?: this.id
        val title = getString("title") ?: return null

        val statusText = getString("status") ?: TripStatus.PLANNING.name

        val status = runCatching {
            TripStatus.valueOf(statusText)
        }.getOrDefault(TripStatus.PLANNING)

        val participants = get("participants") as? List<*>

        return Trip(
            id = id,
            title = title,
            description = getString("description").orEmpty(),
            startDate = getString("startDate").orEmpty(),
            endDate = getString("endDate").orEmpty(),
            status = status,
            ownerId = getString("ownerId").orEmpty(),
            participants = participants
                ?.filterIsInstance<String>()
                ?: emptyList()
        )
    }
    /**
     * Удаляет все документы из подколлекции.
     *
     * Для дипломного прототипа этого достаточно.
     * В больших production-системах удаление больших коллекций
     * обычно делают через Cloud Functions или серверный код.
     */
    private suspend fun deleteCollection(
        collection: CollectionReference
    ) {
        val documents = collection.get().await().documents

        documents.forEach { document ->
            document.reference.delete().await()
        }
    }

    /**
     * Удаляет документы из коллекции по условию.
     *
     * Используется для удаления приглашений и уведомлений,
     * связанных с удаляемой поездкой.
     */
    private suspend fun deleteQueryResult(
        collectionName: String,
        field: String,
        value: String
    ) {
        val documents = firestore
            .collection(collectionName)
            .whereEqualTo(field, value)
            .get()
            .await()
            .documents

        documents.forEach { document ->
            document.reference.delete().await()
        }
    }
}