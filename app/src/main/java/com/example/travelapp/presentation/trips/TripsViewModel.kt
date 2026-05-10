package com.example.travelapp.presentation.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripsViewModel(
    private val authRepository: AuthRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    /**
     * _uiState — внутреннее состояние экрана.
     *
     * Оно private, потому что менять состояние должна только ViewModel.
     */
    private val _uiState = MutableStateFlow(TripsUiState(isLoading = true))

    /**
     * uiState — публичное состояние экрана.
     *
     * Экран может только читать это состояние, но не может менять его напрямую.
     * Это защищает данные от случайных изменений из UI.
     */
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    /**
     * Загружает список поездок текущего пользователя.
     *
     * Сначала получаем id авторизованного пользователя,
     * затем запускаем корутину и подписываемся на поток поездок из репозитория.
     */
    fun loadTrips() {
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = TripsUiState(
                isLoading = false,
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        viewModelScope.launch {
            tripRepository.observeTrips(userId).collect { result ->
                when (result) {
                    is AppResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is AppResult.Success -> {
                        _uiState.value = TripsUiState(
                            isLoading = false,
                            trips = result.data
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