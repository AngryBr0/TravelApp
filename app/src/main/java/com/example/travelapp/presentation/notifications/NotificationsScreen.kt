package com.example.travelapp.presentation.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.NotificationItem

/**
 * NotificationsScreen — экран уведомлений.
 *
 * Здесь пользователь видит события, связанные с поездками:
 * добавление точек маршрута, расходов и участников.
 */
@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Уведомления")

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(text = uiState.errorMessage)
            }

            uiState.notifications.isEmpty() -> {
                Text(text = "Пока уведомлений нет")
            }

            else -> {
                LazyColumn {
                    items(uiState.notifications) { notification ->
                        NotificationCard(notification = notification)
                    }
                }
            }
        }
    }
}

/**
 * Карточка одного уведомления.
 */
@Composable
private fun NotificationCard(
    notification: NotificationItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = notification.text)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = notification.createdAt)
        }
    }
}