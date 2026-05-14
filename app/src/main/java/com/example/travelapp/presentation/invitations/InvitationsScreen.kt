package com.example.travelapp.presentation.invitations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.TripInvitation
import com.example.travelapp.ui.theme.TravelAppTheme

/**
 * InvitationsScreen — экран входящих приглашений.
 *
 * Второй пользователь открывает этот экран и видит,
 * в какие поездки его пригласили.
 */
@Composable
fun InvitationsScreen(
    uiState: InvitationsUiState,
    onBackClick: () -> Unit,
    onAcceptClick: (TripInvitation) -> Unit,
    onDeclineClick: (TripInvitation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Приглашения")
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }
        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(text = uiState.errorMessage)
            }

            uiState.invitations.isEmpty() -> {
                Text(text = "Входящих приглашений нет")
            }

            else -> {
                LazyColumn {
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
 * Карточка одного приглашения.
 */
@Composable
private fun InvitationCard(
    invitation: TripInvitation,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Вас пригласили в поездку:")

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = invitation.tripTitle)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Email: ${invitation.inviteeEmail}")
            Text(text = "Роль: ${invitation.role}")
            Text(text = "Дата: ${invitation.createdAt}")

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                Button(onClick = onAcceptClick) {
                    Text("Принять")
                }

                Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                Button(onClick = onDeclineClick) {
                    Text("Отклонить")
                }
            }
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
                        tripTitle = "Поездка в Санкт-Петербург",
                        inviteeEmail = "friend@example.com",
                        role = ParticipantRole.EDITOR,
                        createdAt = "01.05.2026 12:00"
                    )
                )
            ),
            onBackClick = {},
            onAcceptClick = {},
            onDeclineClick = {}
        )
    }
}