package com.example.travelapp.presentation.trips

import androidx.compose.ui.tooling.preview.Preview
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Trip

/**
 * Экран списка поездок.
 */
@Composable
fun TripsScreen(
    uiState: TripsUiState,
    onCreateTripClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTripClick: (Trip) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Мои поездки")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateTripClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать поездку")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNotificationsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Уведомления")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onInvitationsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Приглашения")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onProfileClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Профиль")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(text = uiState.errorMessage)
            }

            uiState.trips.isEmpty() -> {
                Text(text = "У вас пока нет поездок")
            }

            else -> {
                LazyColumn {
                    items(uiState.trips) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка одной поездки в списке.
 */
@Composable
private fun TripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = trip.title)

            if (trip.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = trip.description)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${trip.startDate} — ${trip.endDate}")
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun TripsScreenPreview() {
    TravelAppTheme {
        TripsScreen(
            uiState = TripsUiState(
                trips = listOf(
                    Trip(
                        id = "1",
                        title = "Поездка в Санкт-Петербург",
                        description = "Музеи, прогулки и достопримечательности",
                        startDate = "01.05.2026",
                        endDate = "05.05.2026"
                    ),
                    Trip(
                        id = "2",
                        title = "Путешествие в Казань",
                        description = "Маршрут на выходные",
                        startDate = "10.06.2026",
                        endDate = "12.06.2026"
                    )
                )
            ),
            onInvitationsClick = {},
            onCreateTripClick = {},
            onNotificationsClick = {},
            onProfileClick = {},
            onTripClick = {}
        )
    }
}