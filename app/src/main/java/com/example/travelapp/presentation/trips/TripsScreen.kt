package com.example.travelapp.presentation.trips

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
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.ui.components.EmptyState
import com.example.travelapp.ui.components.ErrorMessage
import com.example.travelapp.ui.components.TripHomeCard
import com.example.travelapp.ui.components.TripsHomeScaffold
import com.example.travelapp.ui.theme.TravelAppTheme

/**
 * TripsScreen — главный экран после входа.
 *
 * Создание поездки вынесено в плавающую кнопку "+",
 * а в нижнем меню остались только разделы приложения.
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
    TripsHomeScaffold(
        onCreateTripClick = onCreateTripClick,
        onInvitationsClick = onInvitationsClick,
        onNotificationsClick = onNotificationsClick,
        onProfileClick = onProfileClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 18.dp),
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
                        ErrorMessage(message = uiState.errorMessage)
                    }
                }

                uiState.trips.isEmpty() -> {
                    item {
                        EmptyState(
                            text = "У вас пока нет поездок. Нажмите на плюс, чтобы создать первую поездку."
                        )
                    }
                }

                else -> {
                    val activeTrips = uiState.trips.filter { trip ->
                        trip.status != TripStatus.COMPLETED
                    }

                    val completedTrips = uiState.trips.filter { trip ->
                        trip.status == TripStatus.COMPLETED
                    }

                    if (activeTrips.isNotEmpty()) {
                        item {
                            SectionHeader(text = "Активные")
                        }

                        items(activeTrips) { trip ->
                            TripHomeCard(
                                trip = trip,
                                onClick = {
                                    onTripClick(trip)
                                }
                            )
                        }
                    }

                    if (completedTrips.isNotEmpty()) {
                        item {
                            SectionHeader(text = "Завершённые")
                        }

                        items(completedTrips) { trip ->
                            TripHomeCard(
                                trip = trip,
                                onClick = {
                                    onTripClick(trip)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
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
                        title = "Байкал — лето 2026",
                        description = "Путешествие на озеро Байкал",
                        startDate = "10 июля 2026",
                        endDate = "20 июля 2026",
                        status = TripStatus.PLANNING
                    ),
                    Trip(
                        id = "2",
                        title = "Казань — сентябрь",
                        description = "Маршрут на несколько дней",
                        startDate = "5 сент 2026",
                        endDate = "8 сент 2026",
                        status = TripStatus.ACTIVE
                    ),
                    Trip(
                        id = "3",
                        title = "Санкт-Петербург — май",
                        description = "Завершённая поездка",
                        startDate = "1 мая 2026",
                        endDate = "5 мая 2026",
                        status = TripStatus.COMPLETED
                    )
                )
            ),
            onCreateTripClick = {},
            onNotificationsClick = {},
            onInvitationsClick = {},
            onProfileClick = {},
            onTripClick = {}
        )
    }
}