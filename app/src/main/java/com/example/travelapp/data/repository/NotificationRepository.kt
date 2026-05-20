package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun addNotification(notification: NotificationItem): AppResult<Unit>
    fun observeNotifications(userId: String): Flow<AppResult<List<NotificationItem>>>
}