package com.example.travelapp.presentation.participants

import androidx.compose.ui.tooling.preview.Preview
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.ParticipantStatus

/**
 * ParticipantsTab — вкладка участников поездки.
 *
 * Вкладка показывает:
 * - форму приглашения участника;
 * - список участников;
 * - роль и статус каждого участника.
 */
@Composable
fun ParticipantsTab(
    tripId: String,
    uiState: ParticipantsUiState,
    currentUserRole: ParticipantRole,
    canInvite: Boolean,
    onEmailChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Участники поездки")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Ваша роль: $currentUserRole")

        Spacer(modifier = Modifier.height(12.dp))
        if (canInvite) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email участника") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.role,
            onValueChange = onRoleChange,
            label = { Text("Роль: viewer, editor, organizer") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onInviteClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Пригласить участника")
        }
        } else {
            Text(text = "Приглашать участников может только организатор.")
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Список участников")

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.participants.isEmpty()) {
            Text(text = "Пока участники не добавлены")
        } else {
            LazyColumn {
                items(uiState.participants) { participant ->
                    ParticipantCard(participant = participant)
                }
            }
        }
    }
}

/**
 * Карточка одного участника поездки.
 */
@Composable
private fun ParticipantCard(
    participant: TripParticipant
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = participant.email)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Роль: ${participant.role}")

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Статус: ${participant.status}")
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun ParticipantsTabPreview() {
    TravelAppTheme {
        ParticipantsTab(
            tripId = "trip-1",
            uiState = ParticipantsUiState(
                participants = listOf(
                    TripParticipant(
                        id = "user-1",
                        tripId = "trip-1",
                        email = "organizer@example.com",
                        role = ParticipantRole.ORGANIZER,
                        status = ParticipantStatus.ACCEPTED
                    ),
                    TripParticipant(
                        id = "user-2",
                        tripId = "trip-1",
                        email = "editor@example.com",
                        role = ParticipantRole.EDITOR,
                        status = ParticipantStatus.ACCEPTED
                    ),
                    TripParticipant(
                        id = "user-3",
                        tripId = "trip-1",
                        email = "viewer@example.com",
                        role = ParticipantRole.VIEWER,
                        status = ParticipantStatus.INVITED
                    )
                ),
                currentUserRole = ParticipantRole.ORGANIZER,
                canEditTrip = true,
                canInviteParticipants = true
            ),
            currentUserRole = ParticipantRole.ORGANIZER,
            canInvite = true,
            onEmailChange = {},
            onRoleChange = {},
            onInviteClick = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun ParticipantsTabViewerPreview() {
    TravelAppTheme {
        ParticipantsTab(
            tripId = "trip-1",
            uiState = ParticipantsUiState(
                participants = listOf(
                    TripParticipant(
                        id = "user-1",
                        tripId = "trip-1",
                        email = "organizer@example.com",
                        role = ParticipantRole.ORGANIZER,
                        status = ParticipantStatus.ACCEPTED
                    ),
                    TripParticipant(
                        id = "user-3",
                        tripId = "trip-1",
                        email = "viewer@example.com",
                        role = ParticipantRole.VIEWER,
                        status = ParticipantStatus.ACCEPTED
                    )
                ),
                currentUserRole = ParticipantRole.VIEWER,
                canEditTrip = false,
                canInviteParticipants = false
            ),
            currentUserRole = ParticipantRole.VIEWER,
            canInvite = false,
            onEmailChange = {},
            onRoleChange = {},
            onInviteClick = {}
        )
    }
}