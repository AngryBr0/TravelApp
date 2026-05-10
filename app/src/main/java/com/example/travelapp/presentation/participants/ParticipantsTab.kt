package com.example.travelapp.presentation.participants

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ParticipantsTab — вкладка участников поездки.
 *
 * Здесь позже будет список участников и приглашение по email.
 */
@Composable
fun ParticipantsTab(
    tripId: String
) {
    val email = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Участники поездки")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "ID поездки: $tripId")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { newValue ->
                email.value = newValue
            },
            label = { Text("Email участника") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Позже здесь будет добавление участника
        }) {
            Text("Пригласить")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Пока участники не добавлены")
    }
}