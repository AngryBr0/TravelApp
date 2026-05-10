package com.example.travelapp.presentation.budget

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
 * BudgetTab — вкладка бюджета поездки.
 *
 * Здесь позже будет список расходов и общая сумма.
 */
@Composable
fun BudgetTab(
    tripId: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Бюджет поездки")

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "ID поездки: $tripId")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Общая сумма: 0 ₽")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Пока расходы не добавлены")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Позже здесь будет переход на экран добавления расхода
        }) {
            Text("Добавить расход")
        }
    }
}