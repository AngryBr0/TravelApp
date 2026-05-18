package com.example.travelapp.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.travelapp.data.model.TripStatus
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * CreateTripViewModel отвечает за экран создания поездки.
 *
 * Она хранит данные формы и вызывает TripRepository,
 * когда пользователь нажимает кнопку "Создать".
 */
class CreateTripViewModel(
    private val authRepository: AuthRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateStartDate(startDate: String) {
        _uiState.value = _uiState.value.copy(startDate = startDate)
    }

    fun updateEndDate(endDate: String) {
        _uiState.value = _uiState.value.copy(endDate = endDate)
    }

    /**
     * Создает поездку.
     */
    fun createTrip() {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = state.copy(errorMessage = "Пользователь не авторизован")
            return
        }

        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите название поездки")
            return
        }

        if (state.startDate.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Выберите дату начала поездки")
            return
        }

        if (state.endDate.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Выберите дату окончания поездки")
            return
        }

        if (!isDateRangeValid(state.startDate, state.endDate)) {
            _uiState.value = state.copy(
                errorMessage = "Дата начала не может быть позже даты окончания"
            )
            return
        }

        val trip = Trip(
            title = state.title,
            description = state.description,
            startDate = state.startDate,
            endDate = state.endDate,
            ownerId = userId,
            participants = listOf(userId),
            status = calculateInitialTripStatus(
                startDate = state.startDate,
                endDate = state.endDate
            )
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = tripRepository.createTrip(trip)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isCreated = true
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

    private fun calculateInitialTripStatus(
        startDate: String,
        endDate: String
    ): TripStatus {
        val start = parseDate(startDate)
        val end = parseDate(endDate)

        if (start == null || end == null) {
            return TripStatus.PLANNING
        }

        val today = parseDate(
            SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
        ) ?: return TripStatus.PLANNING

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