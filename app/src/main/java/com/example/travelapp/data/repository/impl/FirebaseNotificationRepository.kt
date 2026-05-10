package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseNotificationRepository — реальная реализация NotificationRepository через Firestore.
 *
 * Этот класс отвечает за уведомления пользователя.
 *
 * Структура хранения:
 *
 * notifications/
 *   notificationId/
 *     id
 *     userId
 *     tripId
 *     text
 *     createdAt
 *     createdAtMillis
 *
 * Уведомления хранятся в отдельной коллекции, потому что экран уведомлений
 * должен показывать события пользователя из разных поездок.
 */
class FirebaseNotificationRepository(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    /**
     * Ссылка на коллекцию уведомлений.
     */
    private val notificationsCollection =
        firestore.collection("notifications")

    /**
     * Добавляет новое уведомление в Firestore.
     */
    override suspend fun addNotification(
        notification: NotificationItem
    ): AppResult<Unit> {
        return try {
            if (notification.userId.isBlank()) {
                return AppResult.Error("Не указан пользователь для уведомления")
            }

            if (notification.text.isBlank()) {
                return AppResult.Error("Текст уведомления не может быть пустым")
            }

            val document = notificationsCollection.document()

            /**
             * Если createdAtMillis не передали, ставим текущее системное время.
             */
            val millis = if (notification.createdAtMillis == 0L) {
                System.currentTimeMillis()
            } else {
                notification.createdAtMillis
            }

            val notificationWithId = notification.copy(
                id = document.id,
                createdAtMillis = millis
            )

            val notificationMap = mapOf(
                "id" to notificationWithId.id,
                "userId" to notificationWithId.userId,
                "tripId" to notificationWithId.tripId,
                "text" to notificationWithId.text,
                "createdAt" to notificationWithId.createdAt,
                "createdAtMillis" to notificationWithId.createdAtMillis
            )

            document.set(notificationMap).await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка добавления уведомления"
            )
        }
    }

    /**
     * Подписывается на уведомления конкретного пользователя.
     *
     * whereEqualTo("userId", userId) означает:
     * получить только уведомления текущего пользователя.
     *
     * Сортировку делаем уже в Kotlin, чтобы не упереться в сложные индексы Firestore.
     */
    override fun observeNotifications(
        userId: String
    ): Flow<AppResult<List<NotificationItem>>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки уведомлений"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(AppResult.Success(emptyList()))
                    return@addSnapshotListener
                }

                val notifications = snapshot.documents
                    .mapNotNull { document ->
                        document.toNotificationItemOrNull()
                    }
                    .sortedByDescending { notification ->
                        notification.createdAtMillis
                    }

                trySend(AppResult.Success(notifications))
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
     * Преобразует документ Firestore в объект NotificationItem.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toNotificationItemOrNull(): NotificationItem? {
        val id = getString("id") ?: this.id
        val userId = getString("userId") ?: return null
        val text = getString("text") ?: return null

        return NotificationItem(
            id = id,
            userId = userId,
            tripId = getString("tripId").orEmpty(),
            text = text,
            createdAt = getString("createdAt").orEmpty(),
            createdAtMillis = getLong("createdAtMillis") ?: 0L
        )
    }
}