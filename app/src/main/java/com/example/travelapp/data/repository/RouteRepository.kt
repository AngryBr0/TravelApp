package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.RoutePoint
import kotlinx.coroutines.flow.Flow

/**
 * RouteRepository — интерфейс для работы с точками маршрута.
 *
 * Он описывает операции с маршрутом поездки:
 * добавление точки, получение списка, удаление и изменение порядка.
 */
interface RouteRepository {
    suspend fun addRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit>
    fun observeRoutePoints(tripId: String): Flow<AppResult<List<RoutePoint>>>
    suspend fun updateRoutePointsOrder(
        tripId: String,
        points: List<RoutePoint>
    ): AppResult<Unit>
    suspend fun updateRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit>
    suspend fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ): AppResult<Unit>
}