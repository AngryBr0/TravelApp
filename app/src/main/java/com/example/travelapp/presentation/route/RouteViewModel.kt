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
            searchQuery = query
        )
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description
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
            errorMessage = null
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
                errorMessage = "Выберите место из результатов поиска"
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
                        errorMessage = null
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
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Удаляет точку маршрута.
     */
    fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ) {
        viewModelScope.launch {
            routeRepository.deleteRoutePoint(
                tripId = tripId,
                pointId = pointId
            )
        }
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
    }
}