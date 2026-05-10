package com.example.travelapp.data.model

/**
 * NotificationItem — модель уведомления внутри приложения.
 *
 * Уведомление создается при важных действиях пользователя:
 * - добавлена точка маршрута;
 * - добавлен расход;
 * - приглашен участник.
 *
 * createdAt хранит дату в читаемом виде.
 * createdAtMillis хранит время в миллисекундах, чтобы можно было сортировать уведомления.
 */
data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val tripId: String = "",
    val text: String = "",
    val createdAt: String = "",
    val createdAtMillis: Long = 0L
)