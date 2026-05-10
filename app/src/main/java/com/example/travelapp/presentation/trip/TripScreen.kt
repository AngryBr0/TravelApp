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
import com.example.travelapp.presentation.budget.BudgetUiState
import com.example.travelapp.presentation.map.MapTab
import com.example.travelapp.presentation.participants.ParticipantsTab
import com.example.travelapp.presentation.route.RouteTab
import com.example.travelapp.presentation.route.RouteUiState

/**
 * TripScreen — экран конкретной поездки.
 *
 * Экран объединяет основные разделы работы с поездкой:
 * - маршрут;
 * - карту;
 * - бюджет;
 * - участников.
 *
 * Сам TripScreen не хранит бизнес-логику.
 * Он получает состояние и обработчики событий из ViewModel.
 */
@Composable
fun TripScreen(
    tripId: String,

    routeUiState: RouteUiState,
    onRouteTitleChange: (String) -> Unit,
    onRouteAddressChange: (String) -> Unit,
    onRouteDescriptionChange: (String) -> Unit,
    onRouteLatitudeChange: (String) -> Unit,
    onRouteLongitudeChange: (String) -> Unit,
    onAddRoutePointClick: () -> Unit,
    onDeleteRoutePointClick: (String) -> Unit,

    budgetUiState: BudgetUiState,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetCategoryChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteExpenseClick: (String) -> Unit
) {
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

        when (selectedTabIndex.intValue) {
            0 -> RouteTab(
                tripId = tripId,
                uiState = routeUiState,
                onTitleChange = onRouteTitleChange,
                onAddressChange = onRouteAddressChange,
                onDescriptionChange = onRouteDescriptionChange,
                onLatitudeChange = onRouteLatitudeChange,
                onLongitudeChange = onRouteLongitudeChange,
                onAddPointClick = onAddRoutePointClick,
                onDeletePointClick = onDeleteRoutePointClick
            )

            1 -> MapTab(tripId = tripId)

            2 -> BudgetTab(
                tripId = tripId,
                uiState = budgetUiState,
                onTitleChange = onBudgetTitleChange,
                onCategoryChange = onBudgetCategoryChange,
                onAmountChange = onBudgetAmountChange,
                onAddExpenseClick = onAddExpenseClick,
                onDeleteExpenseClick = onDeleteExpenseClick
            )

            3 -> ParticipantsTab(tripId = tripId)
        }
    }
}