package com.example.travelapp.data.model
/**
 * Trip — модель поездки.
 *
 * Это одна из главных сущностей приложения.
 * Пользователь создает поездку, а внутри нее потом будут:
 * - точки маршрута;
 * - расходы;
 * - участники;
 * - уведомления.
 *
 * data class подходит для хранения данных.
 * Kotlin автоматически создает для него copy(), equals(), hashCode() и toString().
 */
data class Trip(
// Уникальный идентификатор поездки в базе данных Firestore
    val id: String = "",

    // Название поездки, которое вводит пользователь
    val title: String = "",

    // Краткое описание поездки
    val description: String = "",

    // Дата начала поездки
    val startDate: String = "",

    // Дата окончания поездки
    val endDate: String = "",

    // Текущий статус поездки: планируется, активна или завершена
    val status: TripStatus = TripStatus.PLANNING,

    // id пользователя, который создал поездку
    val ownerId: String = "",

    // Список id пользователей, участвующих в поездке
    val participants: List<String> = emptyList()
)