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
                            routePoints = result.data.sortedWith(
                                compareBy<RoutePoint> { point ->
                                    point.dayNumber
                                }.thenBy { point ->
                                    point.order
                                }
                            ),
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
     * Добавляет выбранное место в маршрут выбранного дня.
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

        val selectedDayNumber = state.selectedDayNumber

        val nextOrder = state.routePoints
            .filter { point ->
                point.dayNumber == selectedDayNumber
            }
            .size + 1

        val point = RoutePoint(
            tripId = tripId,
            title = selectedPlace.title,
            address = selectedPlace.address,
            description = state.description,
            latitude = selectedPlace.latitude,
            longitude = selectedPlace.longitude,
            order = nextOrder,
            dayNumber = selectedDayNumber
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
     * Пересчитывает order только внутри выбранного дня,
     * а точки других дней не трогает.
     */
    fun reorderRoutePoints(
        tripId: String,
        reorderedPoints: List<RoutePoint>
    ) {
        val selectedDayNumber = _uiState.value.selectedDayNumber

        val normalizedSelectedDayPoints = reorderedPoints
            .filter { point ->
                point.dayNumber == selectedDayNumber
            }
            .mapIndexed { index, point ->
                point.copy(
                    order = index + 1,
                    dayNumber = selectedDayNumber
                )
            }

        val otherDayPoints = _uiState.value.routePoints.filter { point ->
            point.dayNumber != selectedDayNumber
        }

        val finalPoints = (otherDayPoints + normalizedSelectedDayPoints)
            .sortedWith(
                compareBy<RoutePoint> { point ->
                    point.dayNumber
                }.thenBy { point ->
                    point.order
                }
            )

        saveRouteOrder(
            tripId = tripId,
            points = finalPoints
        )
    }

    /**
     * Удаляет точку маршрута и пересчитывает порядок только внутри её дня.
     */
    fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ) {
        viewModelScope.launch {
            val state = _uiState.value

            val pointToDelete = state.routePoints.firstOrNull { point ->
                point.id == pointId
            }

            if (pointToDelete == null) {
                _uiState.value = state.copy(
                    errorMessage = "Точка маршрута не найдена"
                )
                return@launch
            }

            val targetDayNumber = pointToDelete.dayNumber

            val remainingTargetDayPoints = state.routePoints
                .filter { point ->
                    point.id != pointId && point.dayNumber == targetDayNumber
                }
                .sortedBy { point ->
                    point.order
                }
                .mapIndexed { index, point ->
                    point.copy(order = index + 1)
                }

            val otherDayPoints = state.routePoints.filter { point ->
                point.dayNumber != targetDayNumber
            }

            val finalPoints = (otherDayPoints + remainingTargetDayPoints)
                .sortedWith(
                    compareBy<RoutePoint> { point ->
                        point.dayNumber
                    }.thenBy { point ->
                        point.order
                    }
                )

            when (
                val deleteResult = routeRepository.deleteRoutePoint(
                    tripId = tripId,
                    pointId = pointId
                )
            ) {
                is AppResult.Success -> {
                    routeRepository.updateRoutePointsOrder(
                        tripId = tripId,
                        points = finalPoints
                    )

                    _uiState.value = _uiState.value.copy(
                        routePoints = finalPoints,
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
    fun updateSelectedDay(dayNumber: Int) {
        _uiState.value = _uiState.value.copy(
            selectedDayNumber = dayNumber,
            errorMessage = null
        )
    }
}
