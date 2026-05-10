package com.example.travelapp.presentation.notifications

import com.example.travelapp.data.model.NotificationItem

/**
 * NotificationsUiState — состояние экрана уведомлений.
 *
 * Здесь хранятся:
 * - список уведомлений;
 * - состояние загрузки;
 * - текст ошибки.
 */
data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    val errorMessage: String? = null
)