package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.repository.TripRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FakeTripRepository — временная реализация TripRepository.
 *
 * Данные пока хранятся в памяти приложения.
 * Это нужно, чтобы сначала проверить экраны, ViewModel и навигацию.
 *
 * Позже этот класс заменим на FirebaseTripRepository.
 */
class FakeTripRepository : TripRepository {

    /**
     * MutableStateFlow хранит список поездок и уведомляет экран,
     * когда список изменился.
     */
    private val tripsFlow = MutableStateFlow<AppResult<List<Trip>>>(
        AppResult.Success(emptyList())
    )

    /**
     * Создание новой поездки.
     */
    override suspend fun createTrip(trip: Trip): AppResult<Unit> {
        delay(300)

        val currentTrips = when (val result = tripsFlow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        val newTrip = trip.copy(
            id = System.currentTimeMillis().toString()
        )

        tripsFlow.value = AppResult.Success(currentTrips + newTrip)

        return AppResult.Success(Unit)
    }

    /**
     * Получение списка поездок пользователя.
     *
     * userId пока не используется, потому что это fake-реализация.
     * В Firebase-версии по userId будем получать поездки конкретного пользователя.
     */
    override fun observeTrips(userId: String): Flow<AppResult<List<Trip>>> {
        return tripsFlow
    }

    /**
     * Получение одной поездки по id.
     */
    override fun observeTripById(tripId: String): Flow<AppResult<Trip>> {
        val currentTrips = when (val result = tripsFlow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        val trip = currentTrips.firstOrNull { it.id == tripId }

        return MutableStateFlow(
            if (trip != null) {
                AppResult.Success(trip)
            } else {
                AppResult.Error("Поездка не найдена")
            }
        )
    }

    /**
     * Удаление поездки.
     */
    override suspend fun deleteTrip(tripId: String): AppResult<Unit> {
        val currentTrips = when (val result = tripsFlow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        tripsFlow.value = AppResult.Success(
            currentTrips.filter { trip -> trip.id != tripId }
        )

        return AppResult.Success(Unit)
    }
}