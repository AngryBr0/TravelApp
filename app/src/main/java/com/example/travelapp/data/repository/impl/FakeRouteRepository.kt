package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.repository.RouteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FakeRouteRepository — временная реализация RouteRepository.
 *
 * Сейчас мы еще не подключаем Firebase, поэтому точки маршрута
 * хранятся просто в памяти приложения.
 *
 * Благодаря тому, что класс реализует интерфейс RouteRepository,
 * позже мы сможем заменить его на FirebaseRouteRepository,
 * а ViewModel и экран менять не придется.
 */
class FakeRouteRepository : RouteRepository {

    /**
     * Здесь хранятся точки маршрута по каждой поездке.
     *
     * Ключ Map — это tripId.
     * Значение — поток со списком точек маршрута этой поездки.
     */
    private val routePointsByTrip =
        mutableMapOf<String, MutableStateFlow<AppResult<List<RoutePoint>>>>()

    /**
     * Возвращает Flow для конкретной поездки.
     *
     * Если для поездки еще нет списка точек, создаем пустой список.
     */
    private fun getRoutePointsFlow(
        tripId: String
    ): MutableStateFlow<AppResult<List<RoutePoint>>> {
        return routePointsByTrip.getOrPut(tripId) {
            MutableStateFlow(AppResult.Success(emptyList()))
        }
    }

    /**
     * Добавляет новую точку маршрута в поездку.
     *
     * override означает, что мы реализуем функцию,
     * объявленную в интерфейсе RouteRepository.
     */
    override suspend fun addRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit> {
        delay(300) // имитация небольшой задержки, как при работе с сетью

        val flow = getRoutePointsFlow(tripId)

        val currentPoints = when (val result = flow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        val newPoint = point.copy(
            id = System.currentTimeMillis().toString(),
            tripId = tripId,
            order = currentPoints.size + 1
        )

        flow.value = AppResult.Success(currentPoints + newPoint)

        return AppResult.Success(Unit)
    }

    /**
     * Возвращает поток точек маршрута конкретной поездки.
     *
     * Flow нужен, чтобы экран автоматически обновлялся,
     * когда добавляется или удаляется точка.
     */
    override fun observeRoutePoints(
        tripId: String
    ): Flow<AppResult<List<RoutePoint>>> {
        return getRoutePointsFlow(tripId)
    }

    /**
     * Удаляет точку маршрута по id.
     */
    override suspend fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ): AppResult<Unit> {
        val flow = getRoutePointsFlow(tripId)

        val currentPoints = when (val result = flow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        flow.value = AppResult.Success(
            currentPoints.filter { point ->
                point.id != pointId
            }
        )

        return AppResult.Success(Unit)
    }
}