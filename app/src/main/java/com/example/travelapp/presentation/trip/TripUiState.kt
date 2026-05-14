package com.example.travelapp.presentation.trip

import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripParticipant

/**
 * TripUiState — состояние экрана конкретной поездки.
 *
 * Здесь хранятся:
 * - сама поездка;
 * - данные маршрута, бюджета и участников;
 * - состояние загрузки;
 * - состояние удаления поездки;
 * - ошибка.
 */
data class TripUiState(
    val isLoading: Boolean = false,
    val trip: Trip? = null,

    val routePoints: List<RoutePoint> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val participants: List<TripParticipant> = emptyList(),

    /**
     * true, когда идёт процесс удаления поездки.
     */
    val isDeleting: Boolean = false,

    /**
     * true, когда поездка успешно удалена.
     * По этому признаку AppNavigation вернёт пользователя назад.
     */
    val isDeleted: Boolean = false,

    val errorMessage: String? = null
)