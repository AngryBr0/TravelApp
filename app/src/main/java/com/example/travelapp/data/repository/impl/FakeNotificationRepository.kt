package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.repository.NotificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FakeNotificationRepository — временная реализация NotificationRepository.
 *
 * Пока мы не подключили Firebase, уведомления хранятся в памяти приложения.
 * Позже этот класс заменится на FirebaseNotificationRepository.
 *
 * Экран и ViewModel менять не придется, потому что они работают
 * через интерфейс NotificationRepository.
 */
class FakeNotificationRepository : NotificationRepository {

    /**
     * Хранилище уведомлений по пользователям.
     *
     * Ключ Map — id пользователя.
     * Значение — поток со списком уведомлений этого пользователя.
     */
    private val notificationsByUser =
        mutableMapOf<String, MutableStateFlow<AppResult<List<NotificationItem>>>>()

    /**
     * Получает Flow уведомлений конкретного пользователя.
     *
     * Если уведомлений еще нет, создается пустой список.
     */
    private fun getNotificationsFlow(
        userId: String
    ): MutableStateFlow<AppResult<List<NotificationItem>>> {
        return notificationsByUser.getOrPut(userId) {
            MutableStateFlow(AppResult.Success(emptyList()))
        }
    }

    /**
     * Добавляет новое уведомление.
     */
    override suspend fun addNotification(
        notification: NotificationItem
    ): AppResult<Unit> {
        delay(150)

        val flow = getNotificationsFlow(notification.userId)

        val currentNotifications = when (val result = flow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        val newNotification = notification.copy(
            id = System.currentTimeMillis().toString()
        )

        /**
         * Новые уведомления добавляем в начало списка,
         * чтобы последние события отображались сверху.
         */
        flow.value = AppResult.Success(
            listOf(newNotification) + currentNotifications
        )

        return AppResult.Success(Unit)
    }

    /**
     * Возвращает поток уведомлений пользователя.
     */
    override fun observeNotifications(
        userId: String
    ): Flow<AppResult<List<NotificationItem>>> {
        return getNotificationsFlow(userId)
    }
}