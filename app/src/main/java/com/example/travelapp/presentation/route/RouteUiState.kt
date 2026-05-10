package com.example.travelapp.presentation.route

import com.example.travelapp.data.model.RoutePoint

/**
 * RouteUiState — состояние вкладки маршрута.
 *
 * Здесь хранятся:
 * - список точек маршрута;
 * - значения полей формы;
 * - ошибка;
 * - признак загрузки.
 */
data class RouteUiState(
    val isLoading: Boolean = false,

    val routePoints: List<RoutePoint> = emptyList(),

    val title: String = "",
    val address: String = "",
    val description: String = "",
    val latitude: String = "",
    val longitude: String = "",

    val errorMessage: String? = null
)