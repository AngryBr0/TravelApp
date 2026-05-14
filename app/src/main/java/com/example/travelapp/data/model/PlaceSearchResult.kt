package com.example.travelapp.data.model

/**
 * PlaceSearchResult — результат поиска места через Яндекс.
 *
 * Это ещё не точка маршрута.
 * Это найденный объект, который пользователь может выбрать
 * и потом добавить в маршрут поездки.
 */
data class PlaceSearchResult(
    val title: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)