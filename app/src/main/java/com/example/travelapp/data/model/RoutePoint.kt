package com.example.travelapp.data.model

data class RoutePoint(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val address: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val order: Int = 1,
    val dayNumber: Int = 1
)