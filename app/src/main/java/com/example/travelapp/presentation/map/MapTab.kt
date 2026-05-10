package com.example.travelapp.presentation.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * MapTab — вкладка карты.
 *
 * Позже здесь будет Яндекс.Карта,
 * на которой будут отображаться точки маршрута.
 */
@Composable
fun MapTab(
    tripId: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Карта маршрута")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "ID поездки: $tripId")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Здесь будет карта с точками маршрута")
    }
}