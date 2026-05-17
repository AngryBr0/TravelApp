package com.example.travelapp.presentation.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.NotificationRepository
import com.example.travelapp.data.repository.PlaceSearchRepository
import com.example.travelapp.data.repository.RouteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RouteViewModel отвечает за вкладку маршрута.
 *
 * Теперь логика такая:
 * 1. Пользователь вводит запрос.
 * 2. ViewModel вызывает PlaceSearchRepository.
 * 3. Пользователь выбирает найденное место.
 * 4. ViewModel превращает выбранное место в RoutePoint.
 * 5. RoutePoint сохраняется в Firestore через RouteRepository.
 */
class RouteViewModel(
    private val routeRepository: RouteRepository,
    private val placeSearchRepository: PlaceSearchRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            errorMessage = null,
            isRoutePointAdded = false
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            errorMessage = null,
            isRoutePointAdded = false
        )
    }

    /**
     * Загружает точки маршрута выбранной поездки.
     */
    fun loadRoutePoints(tripId: String) {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            routeRepository.observeRoutePoints(tripId).collect { result ->
                when (result) {
                    AppResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            routePoints = result.data.sortedBy { it.order },
                            errorMessage = null
                        )
                    }

                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Ищет места через Яндекс.
     */
    fun searchPlaces() {
        _uiState.value = _uiState.value.copy(
            isSearching = true,
            errorMessage = null,
            searchResults = emptyList(),
            selectedPlace = null,
            isRoutePointAdded = false
        )
        val query = _uiState.value.searchQuery

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Введите название места"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                errorMessage = null,
                searchResults = emptyList(),
                selectedPlace = null
            )

            when (val result = placeSearchRepository.searchPlaces(query)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = result.data,
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Выбирает место из результатов поиска.
     */
    fun selectPlace(place: PlaceSearchResult) {
        _uiState.value = _uiState.value.copy(
            selectedPlace = place,
            searchResults = emptyList(),
            errorMessage = null,
            isRoutePointAdded = false
        )
    }

    /**
     * Добавляет выбранное место в маршрут.
     */
    fun addSelectedPlaceToRoute(tripId: String) {
        val state = _uiState.value
        val selectedPlace = state.selectedPlace

        if (selectedPlace == null) {
            _uiState.value = state.copy(
                errorMessage = "Выберите место из результатов поиска",
                isRoutePointAdded = false
            )
            return
        }

        val point = RoutePoint(
            tripId = tripId,
            title = selectedPlace.title,
            address = selectedPlace.address,
            description = state.description,
            latitude = selectedPlace.latitude,
            longitude = selectedPlace.longitude,
            order = state.routePoints.size + 1
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = routeRepository.addRoutePoint(tripId, point)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchQuery = "",
                        searchResults = emptyList(),
                        selectedPlace = null,
                        description = "",
                        errorMessage = null,
                        isRoutePointAdded = true
                    )

                    val userId = authRepository.getCurrentUserId()

                    if (userId != null) {
                        notificationRepository.addNotification(
                            NotificationItem(
                                userId = userId,
                                tripId = tripId,
                                text = "Добавлена точка маршрута: ${point.title}",
                                createdAt = getCurrentDateTime()
                            )
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        isRoutePointAdded = false
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Перемещает точку маршрута вверх.
     *
     * Логика:
     * 1. Берем текущий список точек.
     * 2. Сортируем по order.
     * 3. Находим выбранную точку.
     * 4. Меняем её местами с предыдущей.
     * 5. Пересчитываем order.
     * 6. Сохраняем новый порядок в репозиторий.
     */
    fun movePointUp(
        tripId: String,
        pointId: String
    ) {
        val currentPoints = _uiState.value.routePoints
            .sortedBy { point ->
                point.order
            }
            .toMutableList()

        val currentIndex = currentPoints.indexOfFirst { point ->
            point.id == pointId
        }

        if (currentIndex <= 0) {
            return
        }

        val temp = currentPoints[currentIndex - 1]
        currentPoints[currentIndex - 1] = currentPoints[currentIndex]
        currentPoints[currentIndex] = temp

        val reorderedPoints = currentPoints.mapIndexed { index, point ->
            point.copy(order = index + 1)
        }

        saveRouteOrder(tripId, reorderedPoints)
    }

    /**
     * Перемещает точку маршрута вниз.
     */
    fun movePointDown(
        tripId: String,
        pointId: String
    ) {
        val currentPoints = _uiState.value.routePoints
            .sortedBy { point ->
                point.order
            }
            .toMutableList()

        val currentIndex = currentPoints.indexOfFirst { point ->
            point.id == pointId
        }

        if (currentIndex == -1 || currentIndex >= currentPoints.lastIndex) {
            return
        }

        val temp = currentPoints[currentIndex + 1]
        currentPoints[currentIndex + 1] = currentPoints[currentIndex]
        currentPoints[currentIndex] = temp

        val reorderedPoints = currentPoints.mapIndexed { index, point ->
            point.copy(order = index + 1)
        }

        saveRouteOrder(tripId, reorderedPoints)
    }

    /**
     * Сохраняет новый порядок точек маршрута.
     */
    private fun saveRouteOrder(
        tripId: String,
        points: List<RoutePoint>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = routeRepository.updateRoutePointsOrder(
                    tripId = tripId,
                    points = points
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        routePoints = points,
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Редактирует точку маршрута.
     *
     * Пользователь может изменить:
     * - отображаемое название;
     * - заметку к месту.
     */
    fun updateRoutePoint(
        tripId: String,
        pointId: String,
        title: String,
        description: String
    ) {
        val currentPoint = _uiState.value.routePoints.firstOrNull { point ->
            point.id == pointId
        }

        if (currentPoint == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Точка маршрута не найдена"
            )
            return
        }

        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Введите название точки"
            )
            return
        }

        val updatedPoint = currentPoint.copy(
            title = title.trim(),
            description = description.trim()
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = routeRepository.updateRoutePoint(
                    tripId = tripId,
                    point = updatedPoint
                )
            ) {
                is AppResult.Success -> {
                    val updatedPoints = _uiState.value.routePoints.map { point ->
                        if (point.id == pointId) {
                            updatedPoint
                        } else {
                            point
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        routePoints = updatedPoints,
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }
    /**
     * Сохраняет новый порядок точек после перетаскивания.
     *
     * UI передаёт список точек уже в новом порядке.
     * ViewModel пересчитывает order: 1, 2, 3...
     */
    fun reorderRoutePoints(
        tripId: String,
        reorderedPoints: List<RoutePoint>
    ) {
        val normalizedPoints = reorderedPoints.mapIndexed { index, point ->
            point.copy(order = index + 1)
        }

        saveRouteOrder(
            tripId = tripId,
            points = normalizedPoints
        )
    }

    /**
     * Удаляет точку маршрута и пересчитывает порядок оставшихся точек.
     *
     * Это нужно, чтобы после удаления второй точки не было:
     * 1, 3, 4
     *
     * А было:
     * 1, 2, 3
     */
    fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ) {
        viewModelScope.launch {
            val currentPoints = _uiState.value.routePoints
                .sortedBy { point ->
                    point.order
                }

            val remainingPoints = currentPoints
                .filter { point ->
                    point.id != pointId
                }
                .mapIndexed { index, point ->
                    point.copy(order = index + 1)
                }

            when (
                val deleteResult = routeRepository.deleteRoutePoint(
                    tripId = tripId,
                    pointId = pointId
                )
            ) {
                is AppResult.Success -> {
                    routeRepository.updateRoutePointsOrder(
                        tripId = tripId,
                        points = remainingPoints
                    )

                    _uiState.value = _uiState.value.copy(
                        routePoints = remainingPoints,
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = deleteResult.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
    }

    /**
     * Сбрасывает одноразовый флаг успешного добавления точки маршрута.
     */
    fun consumeRoutePointAddedEvent() {
        _uiState.value = _uiState.value.copy(
            isRoutePointAdded = false
        )
    }
}
