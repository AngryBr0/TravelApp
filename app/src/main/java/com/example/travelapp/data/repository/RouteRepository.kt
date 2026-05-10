package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.RoutePoint
import kotlinx.coroutines.flow.Flow

interface RouteRepository {

    suspend fun addRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit>

    fun observeRoutePoints(tripId: String): Flow<AppResult<List<RoutePoint>>>

    suspend fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ): AppResult<Unit>
}