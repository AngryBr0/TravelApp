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
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.TripHomeCard
import com.example.travelapp.ui.components.TripsHomeScaffold
import com.example.travelapp.ui.theme.TravelAppTheme
import java.text.SimpleDateFormat
import java.util.Locale
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
                        AppErrorMessage(message = uiState.errorMessage)
                    }
                }

                uiState.trips.isEmpty() -> {
                    item {
                        AppEmptyState(
                            text = "У вас пока нет поездок. Нажмите на плюс, чтобы создать первую поездку."
                        )
                    }
                }

                else -> {
                    val tripsWithDisplayStatus = uiState.trips.map { trip ->
                        trip.copy(
                            status = calculateTripDisplayStatus(trip)
                        )
                    }

                    val planningTrips = tripsWithDisplayStatus.filter { trip ->
                        trip.status == TripStatus.PLANNING
                    }

                    val activeTrips = tripsWithDisplayStatus.filter { trip ->
                        trip.status == TripStatus.ACTIVE
                    }

                    val completedTrips = tripsWithDisplayStatus.filter { trip ->
                        trip.status == TripStatus.COMPLETED
                    }



                    if (activeTrips.isNotEmpty()) {
                        item {
                            SectionHeader(text = "В процессе")
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

                    if (planningTrips.isNotEmpty()) {
                        item {
                            SectionHeader(text = "Запланированные")
                        }

                        items(planningTrips) { trip ->
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

/**
 * Вычисляет актуальный статус поездки по датам.
 */
private fun calculateTripDisplayStatus(
    trip: Trip
): TripStatus {

    val start = parseTripDate(trip.startDate)
    val end = parseTripDate(trip.endDate)

    if (start == null || end == null) {
        return trip.status
    }

    val todayText = SimpleDateFormat(
        "dd.MM.yyyy",
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    val today = parseTripDate(todayText) ?: return trip.status

    return when {
        today.before(start) -> TripStatus.PLANNING
        today.after(end) -> TripStatus.COMPLETED
        else -> TripStatus.ACTIVE
    }
}

private fun parseTripDate(
    value: String
): java.util.Date? {
    val patterns = listOf(
        "dd.MM.yyyy",
        "yyyy-MM-dd"
    )

    patterns.forEach { pattern ->
        try {
            val formatter = SimpleDateFormat(
                pattern,
                Locale.getDefault()
            )

            formatter.isLenient = false

            return formatter.parse(value)
        } catch (_: Exception) {
        }
    }

    return null
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