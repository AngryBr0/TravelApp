package com.example.travelapp.presentation.route

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
import com.example.travelapp.data.model.RoutePoint

/**
 * RouteTab — вкладка маршрута поездки.
 *
 * Вкладка показывает:
 * - форму добавления точки маршрута;
 * - список уже добавленных точек;
 * - кнопку удаления точки.
 */
@Composable
fun RouteTab(
    tripId: String,
    uiState: RouteUiState,
    onTitleChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onAddPointClick: () -> Unit,
    onDeletePointClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Маршрут поездки")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Название точки") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.address,
            onValueChange = onAddressChange,
            label = { Text("Адрес") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.latitude,
            onValueChange = onLatitudeChange,
            label = { Text("Широта") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.longitude,
            onValueChange = onLongitudeChange,
            label = { Text("Долгота") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onAddPointClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Добавить точку маршрута")
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Точки маршрута")

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.routePoints.isEmpty()) {
            Text(text = "Пока точки маршрута не добавлены")
        } else {
            LazyColumn {
                items(uiState.routePoints) { point ->
                    RoutePointCard(
                        point = point,
                        onDeleteClick = {
                            onDeletePointClick(point.id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Карточка одной точки маршрута.
 */
@Composable
private fun RoutePointCard(
    point: RoutePoint,
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onDeleteClick) {
                Text("Удалить")
            }
        }
    }
}
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
            onTitleChange = {},
            onAddressChange = {},
            onDescriptionChange = {},
            onLatitudeChange = {},
            onLongitudeChange = {},
            onAddPointClick = {},
            onDeletePointClick = {}
        )
    }
}