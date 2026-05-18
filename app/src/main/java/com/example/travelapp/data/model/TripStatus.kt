package com.example.travelapp.data.model

/**
 * TripStatus — состояние поездки.
 *
 * PLANNING — поездка запланирована.
 * ACTIVE — поездка сейчас идёт.
 * COMPLETED — поездка завершена.
 * CANCELLED — поездка отменена.
 */
enum class TripStatus(
    val displayName: String
) {
    PLANNING("Запланирована"),
    ACTIVE("В процессе"),
    COMPLETED("Завершена")
}