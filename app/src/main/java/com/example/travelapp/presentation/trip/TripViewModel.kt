package com.example.travelapp.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.travelapp.data.model.TripStatus
import java.text.SimpleDateFormat
import java.util.Locale
/**
 * TripViewModel отвечает за данные одной конкретной поездки.
 *
 * Она:
 * - загружает поездку по id;
 * - хранит её состояние;
 * - удаляет поездку по запросу организатора.
 */
class TripViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    /**
     * Загружает поездку по tripId.
     */
    fun loadTrip(tripId: String) {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            tripRepository.observeTripById(tripId).collect { result ->
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
                            trip = result.data,
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

    fun updateTrip(
        tripId: String,
        title: String,
        description: String,
        startDate: String,
        endDate: String
    ) {
        val state = _uiState.value
        val currentTrip = state.trip

        if (currentTrip == null) {
            _uiState.value = state.copy(
                errorMessage = "Поездка не найдена"
            )
            return
        }

        if (title.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите название поездки"
            )
            return
        }

        if (startDate.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Выберите дату начала поездки"
            )
            return
        }

        if (endDate.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Выберите дату окончания поездки"
            )
            return
        }

        if (!isDateRangeValid(startDate, endDate)) {
            _uiState.value = state.copy(
                errorMessage = "Дата начала не может быть позже даты окончания"
            )
            return
        }

        val newStatus = calculateTripStatus(
            startDate = startDate,
            endDate = endDate
        )

        val updatedTrip = currentTrip.copy(
            id = tripId,
            title = title.trim(),
            description = description.trim(),
            startDate = startDate,
            endDate = endDate,
            status = newStatus
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = tripRepository.updateTrip(updatedTrip)
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trip = updatedTrip,
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



    /**А
     * Удаляет поездку.
     *
     * Проверка роли выполняется на уровне интерфейса:
     * кнопку удаления видит только ORGANIZER.
     */
    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null
            )

            when (val result = tripRepository.deleteTrip(tripId)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        isDeleted = true
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    private fun isDateRangeValid(
        startDate: String,
        endDate: String
    ): Boolean {
        val start = parseDate(startDate)
        val end = parseDate(endDate)

        if (start == null || end == null) {
            return false
        }

        return !start.after(end)
    }

    private fun calculateTripStatus(
        startDate: String,
        endDate: String
    ): TripStatus {
        val start = parseDate(startDate)
        val end = parseDate(endDate)

        if (start == null || end == null) {
            return TripStatus.PLANNING
        }

        val todayText = SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(System.currentTimeMillis())

        val today = parseDate(todayText) ?: return TripStatus.PLANNING

        return when {
            today.before(start) -> TripStatus.PLANNING
            today.after(end) -> TripStatus.COMPLETED
            else -> TripStatus.ACTIVE
        }
    }

    private fun parseDate(
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
}