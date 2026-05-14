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

    /**
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
}