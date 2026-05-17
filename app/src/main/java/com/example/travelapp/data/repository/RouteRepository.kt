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

    /**
     * Добавить точку маршрута в поездку.
     */
    suspend fun addRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit>

    /**
     * Наблюдать за точками маршрута конкретной поездки.
     */
    fun observeRoutePoints(tripId: String): Flow<AppResult<List<RoutePoint>>>

    /**
     * Обновить порядок точек маршрута.
     *
     * Используется для кнопок ↑ и ↓.
     * Сохраняет новое значение поля order у каждой точки.
     */
    suspend fun updateRoutePointsOrder(
        tripId: String,
        points: List<RoutePoint>
    ): AppResult<Unit>

    /**
     * Обновить точку маршрута.
     *
     * Используется для редактирования названия и заметки.
     */
    suspend fun updateRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit>

    /**
     * Удалить точку маршрута.
     */
    suspend fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ): AppResult<Unit>
}