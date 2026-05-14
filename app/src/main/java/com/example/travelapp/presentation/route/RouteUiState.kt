package com.example.travelapp.presentation.route

import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.data.model.RoutePoint

/**
 * RouteUiState — состояние вкладки маршрута.
 *
 * Теперь пользователь не вводит координаты вручную.
 * Он ищет место через Яндекс, выбирает результат,
 * а координаты берутся автоматически.
 */
data class RouteUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,

    val routePoints: List<RoutePoint> = emptyList(),

    /**
     * Текст, который пользователь вводит в поиске.
     */
    val searchQuery: String = "",

    /**
     * Результаты поиска из Яндекс MapKit.
     */
    val searchResults: List<PlaceSearchResult> = emptyList(),

    /**
     * Выбранный результат поиска.
     */
    val selectedPlace: PlaceSearchResult? = null,

    /**
     * Дополнительная заметка пользователя к месту.
     */
    val description: String = "",

    val errorMessage: String? = null
)