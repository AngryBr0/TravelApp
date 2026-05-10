package com.example.travelapp.presentation.route

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * RouteTab — вкладка маршрута поездки.
 *
 * Здесь позже будет список точек маршрута:
 * достопримечательности, города, адреса и порядок посещения.
 */
@Composable
fun RouteTab(
    tripId: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Маршрут поездки")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "ID поездки: $tripId")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Пока точки маршрута не добавлены")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Позже здесь будет переход на экран добавления точки маршрута
        }) {
            Text("Добавить точку маршрута")
        }
    }
}