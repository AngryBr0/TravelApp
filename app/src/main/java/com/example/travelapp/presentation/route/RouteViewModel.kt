package com.example.travelapp.presentation.route

import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.NotificationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.repository.RouteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * RouteViewModel отвечает за вкладку маршрута.
 *
 * Экран RouteTab не работает с репозиторием напрямую.
 * Он только передает действия пользователя во ViewModel.
 *
 * ViewModel:
 * - хранит состояние экрана;
 * - проверяет введенные данные;
 * - вызывает RouteRepository;
 * - обновляет список точек маршрута.
 */
class RouteViewModel(
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    /**
     * Job нужен, чтобы не запускать несколько подписок на один и тот же маршрут.
     */
    private var observeJob: Job? = null

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLatitude(latitude: String) {
        _uiState.value = _uiState.value.copy(latitude = latitude)
    }

    fun updateLongitude(longitude: String) {
        _uiState.value = _uiState.value.copy(longitude = longitude)
    }

    /**
     * Подписывается на список точек маршрута выбранной поездки.
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
                            routePoints = result.data,
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
     * Добавляет точку маршрута.
     */
    fun addRoutePoint(tripId: String) {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите название точки маршрута"
            )
            return
        }

        val latitude = state.latitude.toDoubleOrNull()
        val longitude = state.longitude.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректные координаты"
            )
            return
        }

        val point = RoutePoint(
            tripId = tripId,
            title = state.title,
            address = state.address,
            description = state.description,
            latitude = latitude,
            longitude = longitude
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
                        title = "",
                        address = "",
                        description = "",
                        latitude = "",
                        longitude = "",
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
    /**
     * Возвращает текущую дату и время в читаемом формате.
     *
     * Используется для отображения времени создания уведомления.
     */
    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
    }
}