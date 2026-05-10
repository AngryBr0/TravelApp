package com.example.travelapp.presentation.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.travelapp.presentation.budget.BudgetTab
import com.example.travelapp.presentation.map.MapTab
import com.example.travelapp.presentation.participants.ParticipantsTab
import com.example.travelapp.presentation.route.RouteTab

/**
 * TripScreen — экран конкретной поездки.
 *
 * Этот экран является центральным для работы с одной поездкой.
 * Он объединяет четыре основные вкладки:
 *
 * 1. Маршрут
 * 2. Карта
 * 3. Бюджет
 * 4. Участники
 *
 * Такая структура соответствует логике ВКР:
 * пользователь работает с маршрутом, картой, расходами и участниками
 * в рамках одной выбранной поездки.
 */
@Composable
fun TripScreen(
    tripId: String
) {
    /**
     * selectedTabIndex хранит номер выбранной вкладки.
     *
     * remember нужен, чтобы Compose запоминал выбранную вкладку
     * при перерисовке экрана.
     */
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Маршрут",
        "Карта",
        "Бюджет",
        "Участники"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        /**
         * TabRow — строка вкладок.
         */
        TabRow(
            selectedTabIndex = selectedTabIndex.intValue
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex.intValue == index,
                    onClick = {
                        selectedTabIndex.intValue = index
                    },
                    text = {
                        Text(text = title)
                    }
                )
            }
        }

        /**
         * В зависимости от выбранной вкладки показываем нужный экран.
         */
        when (selectedTabIndex.intValue) {
            0 -> RouteTab(tripId = tripId)
            1 -> MapTab(tripId = tripId)
            2 -> BudgetTab(tripId = tripId)
            3 -> ParticipantsTab(tripId = tripId)
        }
    }
}