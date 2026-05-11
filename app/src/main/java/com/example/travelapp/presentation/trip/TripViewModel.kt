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

/**
 * TripViewModel отвечает за загрузку данных одной конкретной поездки.
 *
 * Сейчас она нужна, чтобы получить название поездки
 * при создании приглашения участника.
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
}