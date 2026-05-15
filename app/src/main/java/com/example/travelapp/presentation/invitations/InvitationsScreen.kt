package com.example.travelapp.presentation.invitations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripInvitation
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppPrimaryButton
import com.example.travelapp.ui.components.AppScaffold
import com.example.travelapp.ui.components.AppSecondaryButton
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.ui.components.MainTabScaffold
import com.example.travelapp.ui.components.TripsBottomItem
/**
 * InvitationsScreen — экран входящих приглашений.
 */
@Composable
fun InvitationsScreen(
    uiState: InvitationsUiState,
    onTripsClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAcceptClick: (TripInvitation) -> Unit,
    onDeclineClick: (TripInvitation) -> Unit
) {
    MainTabScaffold(
        title = "Приглашения",
        selectedItem = TripsBottomItem.INVITATIONS,
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

                uiState.invitations.isEmpty() -> {
                    item {
                        AppEmptyState(
                            text = "Входящих приглашений пока нет."
                        )
                    }
                }

                else -> {
                    items(uiState.invitations) { invitation ->
                        InvitationCard(
                            invitation = invitation,
                            onAcceptClick = {
                                onAcceptClick(invitation)
                            },
                            onDeclineClick = {
                                onDeclineClick(invitation)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка приглашения.
 */
@Composable
private fun InvitationCard(
    invitation: TripInvitation,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    AppCard {
        Text(
            text = invitation.tripTitle.ifBlank { "Поездка" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AppMutedText(
            text = "Вас пригласили присоединиться к поездке."
        )

        Text(
            text = "Email: ${invitation.inviteeEmail}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Роль: ${invitation.role}",
            style = MaterialTheme.typography.bodyMedium
        )

        if (invitation.createdAt.isNotBlank()) {
            AppMutedText(text = invitation.createdAt)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppPrimaryButton(
                text = "Принять",
                onClick = onAcceptClick,
                modifier = Modifier.weight(1f)
            )

            AppSecondaryButton(
                text = "Отклонить",
                onClick = onDeclineClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InvitationsScreenPreview() {
    TravelAppTheme {
        InvitationsScreen(
            uiState = InvitationsUiState(
                invitations = listOf(
                    TripInvitation(
                        id = "1",
                        tripId = "trip-1",
                        tripTitle = "Байкал — лето 2026",
                        inviteeEmail = "denis@mail.ru",
                        role = ParticipantRole.EDITOR,
                        createdAt = "12.05.2026 14:30"
                    )
                )
            ),
            onTripsClick = {},
            onInvitationsClick = {},
            onNotificationsClick = {},
            onProfileClick = {},
            onAcceptClick = {},
            onDeclineClick = {}
        )
    }
}