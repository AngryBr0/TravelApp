package com.example.travelapp.presentation.route

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppPrimaryButton
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppSmallButton
import com.example.travelapp.ui.components.AppSmallDangerButton
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme

/**
 * RouteTab — вкладка маршрута.
 *
 * Пользователь ищет место через Яндекс, выбирает результат
 * и добавляет его в маршрут.
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
    onMovePointUpClick: (String) -> Unit,
    onMovePointDownClick: (String) -> Unit,
    onDeletePointClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AppSectionTitle(text = "Маршрут поездки")
        }

        if (canEdit) {
            item {
                AppCard {
                    AppSectionTitle(text = "Поиск места")

                    AppMutedText(
                        text = "Введите название места, выберите результат и добавьте его в маршрут."
                    )

                    AppTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = "Место",
                        placeholder = "Например: Казанский собор"
                    )

                    AppPrimaryButton(
                        text = "Найти",
                        onClick = onSearchClick,
                        enabled = !uiState.isSearching
                    )

                    if (uiState.isSearching) {
                        CircularProgressIndicator()
                    }

                    AppErrorMessage(message = uiState.errorMessage)
                }
            }

            if (uiState.searchResults.isNotEmpty()) {
                item {
                    AppSectionTitle(text = "Результаты поиска")
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
                    AppCard {
                        AppSectionTitle(text = "Выбранное место")

                        Text(
                            text = uiState.selectedPlace.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (uiState.selectedPlace.address.isNotBlank()) {
                            AppMutedText(text = uiState.selectedPlace.address)
                        }

                        AppTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            label = "Заметка",
                            placeholder = "Например: посетить в первый день",
                            singleLine = false,
                            maxLines = 3
                        )

                        AppPrimaryButton(
                            text = "Добавить в маршрут",
                            onClick = onAddSelectedPlaceClick,
                            enabled = !uiState.isLoading
                        )

                        if (uiState.isLoading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        } else {
            item {
                AppEmptyState(
                    text = "У вас режим просмотра. Редактирование маршрута недоступно."
                )
            }
        }

        item {
            AppSectionTitle(text = "Точки маршрута")
        }

        if (uiState.routePoints.isEmpty()) {
            item {
                AppEmptyState(text = "Пока точки маршрута не добавлены.")
            }
        } else {
            val sortedPoints = uiState.routePoints.sortedBy { point ->
                point.order
            }

            itemsIndexed(sortedPoints) { index, point ->
                RoutePointCard(
                    point = point,
                    canEdit = canEdit,
                    canMoveUp = index > 0,
                    canMoveDown = index < sortedPoints.lastIndex,
                    onMoveUpClick = {
                        onMovePointUpClick(point.id)
                    },
                    onMoveDownClick = {
                        onMovePointDownClick(point.id)
                    },
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
 */
@Composable
private fun SearchResultCard(
    place: PlaceSearchResult,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = place.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (place.address.isNotBlank()) {
            AppMutedText(text = place.address)
        }

        AppMutedText(text = "Нажмите, чтобы выбрать")
    }
}

/**
 * Карточка точки маршрута.
 */
@Composable
private fun RoutePointCard(
    point: RoutePoint,
    canEdit: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AppCard {
        Text(
            text = "${point.order}. ${point.title}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (point.address.isNotBlank()) {
            AppMutedText(text = point.address)
        }

        if (point.description.isNotBlank()) {
            Text(
                text = point.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (canEdit) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSmallButton(
                    text = "↑",
                    onClick = onMoveUpClick,
                    enabled = canMoveUp
                )

                AppSmallButton(
                    text = "↓",
                    onClick = onMoveDownClick,
                    enabled = canMoveDown
                )

                AppSmallDangerButton(
                    text = "Удалить",
                    onClick = onDeleteClick
                )
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
                searchQuery = "Эрмитаж",
                searchResults = listOf(
                    PlaceSearchResult(
                        title = "Государственный Эрмитаж",
                        address = "Санкт-Петербург, Дворцовая площадь, 2",
                        latitude = 59.9398,
                        longitude = 30.3146
                    )
                ),
                routePoints = listOf(
                    RoutePoint(
                        id = "1",
                        tripId = "trip-1",
                        title = "Эрмитаж",
                        address = "Санкт-Петербург",
                        description = "Посетить утром",
                        latitude = 59.9398,
                        longitude = 30.3146,
                        order = 1
                    ),
                    RoutePoint(
                        id = "2",
                        tripId = "trip-1",
                        title = "Казанский собор",
                        address = "Санкт-Петербург",
                        description = "",
                        latitude = 59.9343,
                        longitude = 30.3245,
                        order = 2
                    )
                )
            ),
            canEdit = true,
            onSearchQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onDescriptionChange = {},
            onAddSelectedPlaceClick = {},
            onMovePointUpClick = {},
            onMovePointDownClick = {},
            onDeletePointClick = {}
        )
    }
}