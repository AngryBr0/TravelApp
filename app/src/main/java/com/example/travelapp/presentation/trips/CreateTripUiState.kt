package com.example.travelapp.presentation.trips

/**
 * Состояние экрана создания поездки.
 *
 * Здесь хранятся значения полей формы:
 * название, описание, даты, ошибка и признак успешного создания.
 */
data class CreateTripUiState(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCreated: Boolean = false
)