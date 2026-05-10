package com.example.travelapp.presentation.trip

import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripParticipant

data class TripUiState(
    val isLoading: Boolean = false,
    val trip: Trip? = null,
    val routePoints: List<RoutePoint> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val participants: List<TripParticipant> = emptyList(),
    val errorMessage: String? = null
)