package com.example.travelapp.presentation.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.travelapp.ui.components.AppBottomActionButton
import com.example.travelapp.ui.components.AppDangerButton
import androidx.compose.foundation.lazy.LazyRow

/**
 * RouteTab — вкладка маршрута.
 *
 * Порядок точек меняется через drag-and-drop:
 * пользователь зажимает иконку перетаскивания справа
 * и переносит точку на нужное место.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteTab(
    tripId: String,
    uiState: RouteUiState,
    daysCount: Int,
    tripStartDate: String,
    onSelectedDayChange: (Int) -> Unit,
    canEdit: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onPlaceClick: (PlaceSearchResult) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddSelectedPlaceClick: () -> Unit,
    onRoutePointAddedHandled: () -> Unit,
    onEditPointClick: (String, String, String) -> Unit,
    onReorderPoints: (List<RoutePoint>) -> Unit,
    onDeletePointClick: (String) -> Unit
) {
    val isAddPlaceSheetVisible = remember { mutableStateOf(false) }

    var editingPoint by remember {
        mutableStateOf<RoutePoint?>(null)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val localPoints = remember {
        mutableStateListOf<RoutePoint>()
    }

    val lazyListState = rememberLazyListState()

    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState
    ) { from, to ->
        localPoints.add(
            index = to.index,
            element = localPoints.removeAt(from.index)
        )
    }

    LaunchedEffect(
        uiState.routePoints,
        uiState.selectedDayNumber
    ) {
        localPoints.clear()
        localPoints.addAll(
            uiState.routePoints
                .filter { point ->
                    point.dayNumber == uiState.selectedDayNumber
                }
                .sortedBy { point ->
                    point.order
                }
        )
    }

    LaunchedEffect(uiState.isRoutePointAdded) {
        if (uiState.isRoutePointAdded) {
            isAddPlaceSheetVisible.value = false
            onRoutePointAddedHandled()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (daysCount > 1) {
                RouteDaySelector(
                    daysCount = daysCount,
                    selectedDayNumber = uiState.selectedDayNumber,
                    tripStartDate = tripStartDate,
                    onDayClick = onSelectedDayChange
                )
            }

            AppSectionTitle(text = "Маршрут")

            if (localPoints.isEmpty()) {
                AppEmptyState(
                    text = if (canEdit) {
                        "Пока точки маршрута не добавлены. Нажмите “Добавить место”, чтобы начать маршрут."
                    } else {
                        "Пока точки маршрута не добавлены."
                    }
                )
            } else {


                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = localPoints,
                        key = { point ->
                            point.id
                        }
                    ) { point ->
                        ReorderableItem(
                            state = reorderableLazyListState,
                            key = point.id
                        ) { isDragging ->
                            val displayOrder = localPoints.indexOfFirst { currentPoint ->
                                currentPoint.id == point.id
                            } + 1

                            RoutePointRow(
                                point = point,
                                displayOrder = displayOrder,
                                canEdit = canEdit,
                                elevation = if (isDragging) 8.dp else 1.dp,
                                dragHandleModifier = if (canEdit) {
                                    with(this) {
                                        Modifier.draggableHandle(
                                            onDragStopped = {
                                                onReorderPoints(localPoints.toList())
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                },
                                onClick = {
                                    if (canEdit) {
                                        editingPoint = point
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (!canEdit) {
                AppEmptyState(
                    text = "У вас режим просмотра. Редактирование маршрута недоступно."
                )
            }
        }

        if (canEdit) {
            AppBottomActionButton(
                text = "Добавить место",
                icon = Icons.Filled.AddLocationAlt,
                onClick = {
                    isAddPlaceSheetVisible.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .zIndex(1f),
                width = 230.dp
            )
        }
    }

    if (isAddPlaceSheetVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isAddPlaceSheetVisible.value = false
            },
            sheetState = sheetState
        ) {
            AddRoutePointSheetContent(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onSearchClick = onSearchClick,
                onPlaceClick = onPlaceClick,
                onDescriptionChange = onDescriptionChange,
                onAddSelectedPlaceClick = onAddSelectedPlaceClick
            )
        }
    }

    if (editingPoint != null) {
        ModalBottomSheet(
            onDismissRequest = {
                editingPoint = null
            },
            sheetState = sheetState
        ) {
            EditRoutePointSheetContent(
                point = editingPoint!!,
                isLoading = uiState.isLoading,
                onSaveClick = { title, description ->
                    onEditPointClick(
                        editingPoint!!.id,
                        title,
                        description
                    )

                    editingPoint = null
                },
                onDeleteClick = {
                    onDeletePointClick(editingPoint!!.id)
                    editingPoint = null
                }
            )
        }
    }
}

/**
 * Компактный переключатель дат маршрута.
 *
 * Пользователь видит дату, например "12 мая",
 * а selectedDayNumber используется только внутри логики.
 */
@Composable
private fun RouteDaySelector(
    daysCount: Int,
    selectedDayNumber: Int,
    tripStartDate: String,
    onDayClick: (Int) -> Unit
) {
    val safeDaysCount = daysCount.coerceAtLeast(1)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(safeDaysCount) { index ->
            val dayNumber = index + 1
            val isSelected = dayNumber == selectedDayNumber

            Button(
                onClick = {
                    onDayClick(dayNumber)
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) {
                        Color(0xFF2563EB)
                    } else {
                        Color(0xFFE5E7EB)
                    },
                    contentColor = if (isSelected) {
                        Color.White
                    } else {
                        Color(0xFF111827)
                    }
                )
            ) {
                Text(
                    text = routeDateText(
                        startDate = tripStartDate,
                        dayNumber = dayNumber
                    ),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Bottom sheet добавления места.
 */
@Composable
private fun AddRoutePointSheetContent(
    uiState: RouteUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onPlaceClick: (PlaceSearchResult) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddSelectedPlaceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionTitle(text = "Добавить место")


        AppTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = "Поиск",
            placeholder = "Например: Эрмитаж"
        )

        Button(
            onClick = onSearchClick,
            enabled = !uiState.isSearching,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Найти",
                    modifier = Modifier.padding(end = 6.dp)
                )

                Text(
                    text = "Найти",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.isSearching) {
            CircularProgressIndicator()
        }

        if (uiState.searchResults.isNotEmpty()) {
            Text(
                text = "Результаты поиска",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn(
                modifier = Modifier.heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.searchResults) { place ->
                    SearchResultRow(
                        place = place,
                        onClick = {
                            onPlaceClick(place)
                        }
                    )
                }
            }
        }

        if (uiState.selectedPlace != null) {
            SelectedPlaceCard(
                place = uiState.selectedPlace
            )

            AppTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = "Заметка",
                placeholder = "Необязательно",
                singleLine = false,
                maxLines = 3
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            AppErrorMessage(message = uiState.errorMessage)
        }

        Button(
            onClick = onAddSelectedPlaceClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Добавить в маршрут",
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Bottom sheet редактирования точки маршрута.
 *
 * Здесь же находится удаление точки,
 * чтобы кнопка удаления не была на поверхности списка.
 */
@Composable
private fun EditRoutePointSheetContent(
    point: RoutePoint,
    isLoading: Boolean,
    onSaveClick: (String, String) -> Unit,
    onDeleteClick: () -> Unit
) {
    var title by remember(point.id) {
        mutableStateOf(point.title)
    }

    var description by remember(point.id) {
        mutableStateOf(point.description)
    }

    var localError by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionTitle(text = "Настройки точки")

        AppMutedText(
            text = "Измените название или заметку к месту."
        )

        AppTextField(
            value = title,
            onValueChange = {
                title = it
                localError = null
            },
            label = "Название",
            placeholder = "Название точки"
        )

        AppTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = "Заметка",
            placeholder = "Необязательно",
            singleLine = false,
            maxLines = 3
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            AppErrorMessage(message = localError)
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    localError = "Введите название точки"
                } else {
                    onSaveClick(title, description)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Сохранить",
                fontWeight = FontWeight.Bold
            )
        }

        AppDangerButton(
            text = "Удалить точку",
            onClick = onDeleteClick,
            enabled = !isLoading
        )

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Результат поиска места.
 */
@Composable
private fun SearchResultRow(
    place: PlaceSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE5E7EB)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = place.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (place.address.isNotBlank()) {
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Карточка выбранного места.
 */
@Composable
private fun SelectedPlaceCard(
    place: PlaceSearchResult
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF6FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Выбрано",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = place.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (place.address.isNotBlank()) {
                AppMutedText(text = place.address)
            }
        }
    }
}

/**
 * Карточка точки маршрута.
 *
 * Нажатие на карточку открывает настройки точки.
 * Удаление вынесено внутрь настроек.
 */
@Composable
private fun RoutePointRow(
    point: RoutePoint,
    displayOrder: Int,
    canEdit: Boolean,
    elevation: Dp,
    dragHandleModifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canEdit,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE5E7EB)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "$displayOrder.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = point.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (point.address.isNotBlank()) {
                        Text(
                            text = point.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (point.description.isNotBlank()) {
                        Text(
                            text = point.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF374151),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (canEdit) {
                    IconButton(
                        onClick = {},
                        modifier = dragHandleModifier
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DragIndicator,
                            contentDescription = "Перетащить",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Возвращает дату маршрута для интерфейса.
 *
 * Например:
 * 12 мая
 * 13 мая
 * 14 мая
 *
 * Год не показываем, чтобы переключатель выглядел легче.
 */
private fun routeDateText(
    startDate: String,
    dayNumber: Int
): String {
    if (startDate.isBlank()) {
        return "Дата не указана"
    }

    val patterns = listOf(
        "dd.MM.yyyy",
        "yyyy-MM-dd"
    )

    patterns.forEach { pattern ->
        try {
            val formatter = java.text.SimpleDateFormat(
                pattern,
                java.util.Locale.getDefault()
            )

            val date = formatter.parse(startDate)

            if (date != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = date
                calendar.add(
                    java.util.Calendar.DAY_OF_MONTH,
                    dayNumber - 1
                )

                val outputFormatter = java.text.SimpleDateFormat(
                    "d MMMM",
                    java.util.Locale("ru")
                )

                return outputFormatter.format(calendar.time)
            }
        } catch (_: Exception) {
        }
    }

    return "Дата не указана"
}

@Preview(
    showBackground = true,
    name = "RouteTab - список точек"
)
@Composable
private fun RouteTabPreview() {
    TravelAppTheme {
        RouteTab(
            tripId = "trip-1",
            uiState = RouteUiState(
                routePoints = listOf(
                    RoutePoint(
                        id = "1",
                        tripId = "trip-1",
                        title = "Государственный Эрмитаж",
                        address = "Санкт-Петербург, Дворцовая площадь, 2",
                        description = "Посетить утром",
                        latitude = 59.9398,
                        longitude = 30.3146,
                        order = 1
                    ),
                    RoutePoint(
                        id = "2",
                        tripId = "trip-1",
                        title = "Казанский собор",
                        address = "Санкт-Петербург, Казанская площадь, 2",
                        description = "",
                        latitude = 59.9343,
                        longitude = 30.3245,
                        order = 2
                    ),
                    RoutePoint(
                        id = "3",
                        tripId = "trip-1",
                        title = "Исаакиевский собор",
                        address = "Санкт-Петербург, Исаакиевская площадь, 4",
                        description = "Можно зайти после обеда",
                        latitude = 59.9340,
                        longitude = 30.3061,
                        order = 3
                    )
                )
            ),
            canEdit = true,
            onSearchQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onDescriptionChange = {},
            onAddSelectedPlaceClick = {},
            onRoutePointAddedHandled = {},
            onEditPointClick = { _, _, _ -> },
            daysCount = 3,
            tripStartDate = "12.05.2026",
            onSelectedDayChange = {},
            onReorderPoints = {},
            onDeletePointClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "RouteTab - пустой маршрут"
)
@Composable
private fun RouteTabEmptyPreview() {
    TravelAppTheme {
        RouteTab(
            tripId = "trip-1",
            uiState = RouteUiState(),
            canEdit = true,
            onSearchQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onDescriptionChange = {},
            onAddSelectedPlaceClick = {},
            onRoutePointAddedHandled = {},
            onEditPointClick = { _, _, _ -> },
            onReorderPoints = {},
            daysCount = 3,
            tripStartDate = "12.05.2026",
            onSelectedDayChange = {},
            onDeletePointClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "RouteTab - окно добавления"
)
@Composable
private fun AddRoutePointSheetPreview() {
    TravelAppTheme {
        AddRoutePointSheetContent(
            uiState = RouteUiState(
                searchQuery = "Эрмитаж",
                searchResults = listOf(
                    PlaceSearchResult(
                        title = "Государственный Эрмитаж",
                        address = "Санкт-Петербург, Дворцовая площадь, 2",
                        latitude = 59.9398,
                        longitude = 30.3146
                    ),
                    PlaceSearchResult(
                        title = "Главный штаб Эрмитажа",
                        address = "Санкт-Петербург, Дворцовая площадь, 6-8",
                        latitude = 59.9392,
                        longitude = 30.3180
                    )
                ),
                selectedPlace = PlaceSearchResult(
                    title = "Государственный Эрмитаж",
                    address = "Санкт-Петербург, Дворцовая площадь, 2",
                    latitude = 59.9398,
                    longitude = 30.3146
                ),
                description = "Посетить утром"
            ),
            onSearchQueryChange = {},
            onSearchClick = {},
            onPlaceClick = {},
            onDescriptionChange = {},
            onAddSelectedPlaceClick = {}
        )
    }
}