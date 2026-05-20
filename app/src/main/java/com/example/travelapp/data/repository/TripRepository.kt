package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Trip
import kotlinx.coroutines.flow.Flow
import com.example.travelapp.data.model.TripStatus

/**
 * TripRepository — интерфейс для работы с поездками.
 *
 * Репозиторий скрывает источник данных от остального приложения.
 * ViewModel не должна знать, где именно хранятся поездки:
 * в Firebase, локальной базе или тестовом списке.
 *
 * Благодаря интерфейсу позже можно заменить реализацию,
 * не меняя код экранов и ViewModel.
 */
interface TripRepository {

    /**
     * Создает новую поездку.
     *
     * suspend означает, что функция выполняется асинхронно
     * и может быть вызвана внутри корутины.
     */
    suspend fun createTrip(trip: Trip): AppResult<Unit>

    /**
     * Наблюдает за списком поездок пользователя.
     *
     * Flow используется потому, что данные могут изменяться со временем.
     * Например, пользователь создал новую поездку — список автоматически обновился.
     */
    fun observeTrips(userId: String): Flow<AppResult<List<Trip>>>

    /**
     * Наблюдает за одной конкретной поездкой по ее id.
     */
    fun observeTripById(tripId: String): Flow<AppResult<Trip>>

    suspend fun updateTrip(
        trip: Trip
    ): AppResult<Unit>

    /**
     * Удаляет поездку.
     */
    suspend fun deleteTrip(tripId: String): AppResult<Unit>
}