package com.example.travelapp.presentation.route

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.data.model.RoutePoint

/**
 * RouteTab — вкладка маршрута поездки.
 *
 * Вся вкладка сделана через LazyColumn, чтобы экран можно было прокручивать.
 * Это важно, потому что результатов поиска может быть много.
 */
@Composable
fun RouteTab(
    tripId: String,
    uiState: RouteUiState,
    canEdit: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onPlaceClick: (PlaceSearchResult) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddSelectedPlaceClick: () -> Unit,
    onDeletePointClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(text = "Маршрут поездки")

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (canEdit) {
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Найти место") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSearching
                ) {
                    Text("Найти")
                }

                if (uiState.isSearching) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = uiState.errorMessage)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.searchResults.isNotEmpty()) {
                item {
                    Text(text = "Результаты поиска")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(uiState.searchResults) { place ->
                    SearchResultCard(
                        place = place,
                        onClick = {
                            onPlaceClick(place)
                        }
                    )
                }
            }

            if (uiState.selectedPlace != null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Выбрано:")
                    Text(text = uiState.selectedPlace.title)

                    if (uiState.selectedPlace.address.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = uiState.selectedPlace.address)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Заметка к месту") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onAddSelectedPlaceClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        Text("Добавить в маршрут")
                    }

                    if (uiState.isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            item {
                Text(text = "У вас режим просмотра. Редактирование маршрута недоступно.")
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        item {
            Text(text = "Точки маршрута")
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.routePoints.isEmpty()) {
            item {
                Text(text = "Пока точки маршрута не добавлены")
            }
        } else {
            items(uiState.routePoints.sortedBy { it.order }) { point ->
                RoutePointCard(
                    point = point,
                    canDelete = canEdit,
                    onDeleteClick = {
                        onDeletePointClick(point.id)
                    }
                )
            }
        }
    }
}

/**
 * Карточка результата поиска.
 *
 * Пользователь нажимает на карточку, чтобы выбрать место.
 */
@Composable
private fun SearchResultCard(
    place: PlaceSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = place.title)

            if (place.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = place.address)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Нажмите, чтобы выбрать")
        }
    }
}

/**
 * Карточка точки маршрута.
 */
@Composable
private fun RoutePointCard(
    point: RoutePoint,
    canDelete: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "${point.order}. ${point.title}")

            if (point.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = point.address)
            }

            if (point.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = point.description)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Координаты: ${point.latitude}, ${point.longitude}"
            )

            if (canDelete) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onDeleteClick) {
                    Text("Удалить")
                }
            }
        }
    }
}
/*
@Preview(showBackground = true)
@Composable
private fun RouteTabPreview() {
    TravelAppTheme {
        RouteTab(
            tripId = "trip-1",
            uiState = RouteUiState(
                routePoints = listOf(
                    RoutePoint(
                        id = "1",
                        title = "Эрмитаж",
                        address = "Санкт-Петербург",
                        description = "Музей",
                        latitude = 59.9398,
                        longitude = 30.3146,
                        order = 1
                    ),
                    RoutePoint(
                        id = "2",
                        title = "Казанский собор",
                        address = "Санкт-Петербург",
                        description = "Достопримечательность",
                        latitude = 59.9343,
                        longitude = 30.3245,
                        order = 2
                    )
                )
            ),
            onDescriptionChange = {},
            onDeletePointClick = {},
            canEdit = true,
            onSearchQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onAddSelectedPlaceClick = {}
        )
    }
}
*/