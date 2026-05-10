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

        val trip = Trip(
            title = state.title,
            description = state.description,
            startDate = state.startDate,
            endDate = state.endDate,
            ownerId = userId,
            participants = listOf(userId)
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
}