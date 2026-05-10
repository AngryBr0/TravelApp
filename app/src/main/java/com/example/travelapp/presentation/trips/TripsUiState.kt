package com.example.travelapp.presentation.trips

import com.example.travelapp.data.model.Trip

data class TripsUiState(
    val isLoading: Boolean = false,
    val trips: List<Trip> = emptyList(),
    val errorMessage: String? = null
)