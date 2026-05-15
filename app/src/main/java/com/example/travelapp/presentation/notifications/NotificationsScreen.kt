package com.example.travelapp.presentation.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppScaffold
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.ui.components.MainTabScaffold
import com.example.travelapp.ui.components.TripsBottomItem
/**
 * NotificationsScreen — экран уведомлений.
 */
@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onTripsClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    MainTabScaffold(
        title = "Уведомления",
        selectedItem = TripsBottomItem.NOTIFICATIONS,
        onTripsClick = onTripsClick,
        onInvitationsClick = onInvitationsClick,
        onNotificationsClick = onNotificationsClick,
        onProfileClick = onProfileClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                uiState.isLoading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    item {
                        AppErrorMessage(message = uiState.errorMessage)
                    }
                }

                uiState.notifications.isEmpty() -> {
                    item {
                        AppEmptyState(
                            text = "Пока уведомлений нет."
                        )
                    }
                }

                else -> {
                    items(uiState.notifications) { notification ->
                        NotificationCard(notification = notification)
                    }
                }
            }
        }
    }
}

/**
 * Карточка уведомления.
 */
@Composable
private fun NotificationCard(
    notification: NotificationItem
) {
    AppCard {
        Text(
            text = notification.text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )

        if (notification.createdAt.isNotBlank()) {
            AppMutedText(text = notification.createdAt)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    TravelAppTheme {
        NotificationsScreen(
            uiState = NotificationsUiState(
                notifications = listOf(
                    NotificationItem(
                        id = "1",
                        userId = "user-1",
                        tripId = "trip-1",
                        text = "Добавлена точка маршрута: Казанский собор",
                        createdAt = "12.05.2026 15:10"
                    ),
                    NotificationItem(
                        id = "2",
                        userId = "user-1",
                        tripId = "trip-1",
                        text = "Приглашён участник: friend@mail.ru",
                        createdAt = "12.05.2026 14:40"
                    )
                )
            ),
            onTripsClick = {},
            onInvitationsClick = {},
            onNotificationsClick = {},
            onProfileClick = {}
        )
    }
}