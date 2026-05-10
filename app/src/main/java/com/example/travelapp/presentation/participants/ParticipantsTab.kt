package com.example.travelapp.presentation.participants

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
    onEmailChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Участники поездки")

        Spacer(modifier = Modifier.height(12.dp))

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