package com.example.travelapp.presentation.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.presentation.budget.BudgetTab
import com.example.travelapp.presentation.budget.BudgetUiState
import com.example.travelapp.presentation.map.MapTab
import com.example.travelapp.presentation.participants.ParticipantsTab
import com.example.travelapp.presentation.participants.ParticipantsUiState
import com.example.travelapp.presentation.route.RouteTab
import com.example.travelapp.presentation.route.RouteUiState
import com.example.travelapp.ui.components.AppScaffold
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.model.ExpenseOwnerType
import com.example.travelapp.presentation.budget.ExpenseSortType

/**
 * TripScreen — экран конкретной поездки.
 *
 * Здесь убран старый TabRow.
 * Вместо него используется более современный горизонтальный переключатель вкладок.
 */
@Composable
fun TripScreen(
    tripId: String,
    onBackClick: () -> Unit,

    tripTitle: String,
    tripStartDate: String,
    tripEndDate: String,
    isDeletingTrip: Boolean,
    tripErrorMessage: String?,
    canDeleteTrip: Boolean,
    onDeleteTripClick: () -> Unit,

    routeUiState: RouteUiState,
    onRouteSearchQueryChange: (String) -> Unit,
    onRouteSearchClick: () -> Unit,
    onRoutePlaceClick: (PlaceSearchResult) -> Unit,
    onRouteDescriptionChange: (String) -> Unit,
    onAddSelectedPlaceClick: () -> Unit,
    onRoutePointAddedHandled: () -> Unit,
    onEditRoutePointClick: (String, String, String) -> Unit,
    onReorderRoutePoints: (List<RoutePoint>) -> Unit,
    onRouteSelectedDayChange: (Int) -> Unit,
    onDeleteRoutePointClick: (String) -> Unit,

    budgetUiState: BudgetUiState,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetCategoryChange: (ExpenseCategory) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetExpenseDateChange: (String) -> Unit,
    onBudgetOwnerTypeChange: (ExpenseOwnerType) -> Unit,
    onBudgetOwnerUserChange: (String, String) -> Unit,
    onBudgetSortTypeChange: (ExpenseSortType) -> Unit,
    onBudgetLimitInputChange: (String) -> Unit,
    onSaveBudgetLimitClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseAddedHandled: () -> Unit,
    onUpdateExpenseClick: (String, String, ExpenseCategory, String, String, ExpenseOwnerType, String, String) -> Unit,
    onDeleteExpenseClick: (String) -> Unit,

    participantsUiState: ParticipantsUiState,
    onParticipantEmailChange: (String) -> Unit,
    onParticipantRoleChange: (String) -> Unit,
    onInviteParticipantClick: () -> Unit,
    onUpdateParticipantRoleClick: (String, ParticipantRole) -> Unit,
    onDeleteParticipantClick: (String, String) -> Unit,
) {
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showActionsMenu = remember { mutableStateOf(false) }

    val tabs = listOf(
        "Маршрут",
        "Карта",
        "Бюджет",
        "Участники"
    )

    AppScaffold(
        title = tripTitle.ifBlank { "Поездка" },
        onBackClick = onBackClick,
        actions = {
            if (canDeleteTrip) {
                Box(
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = {
                            showActionsMenu.value = true
                        },
                        enabled = !isDeletingTrip
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Действия с поездкой"
                        )
                    }

                    DropdownMenu(
                        expanded = showActionsMenu.value,
                        onDismissRequest = {
                            showActionsMenu.value = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Удалить поездку",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showActionsMenu.value = false
                                showDeleteDialog.value = true
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            TripTabs(
                tabs = tabs,
                selectedIndex = selectedTabIndex.intValue,
                onTabClick = { index ->
                    selectedTabIndex.intValue = index
                }
            )

            when (selectedTabIndex.intValue) {
                0 -> RouteTab(
                    tripId = tripId,
                    uiState = routeUiState,
                    daysCount = calculateTripDaysCount(
                        startDate = tripStartDate,
                        endDate = tripEndDate
                    ),
                    tripStartDate = tripStartDate,
                    onSelectedDayChange = onRouteSelectedDayChange,
                    canEdit = participantsUiState.canEditTrip,
                    onSearchQueryChange = onRouteSearchQueryChange,
                    onSearchClick = onRouteSearchClick,
                    onPlaceClick = onRoutePlaceClick,
                    onDescriptionChange = onRouteDescriptionChange,
                    onAddSelectedPlaceClick = onAddSelectedPlaceClick,
                    onRoutePointAddedHandled = onRoutePointAddedHandled,
                    onEditPointClick = onEditRoutePointClick,
                    onReorderPoints = onReorderRoutePoints,
                    onDeletePointClick = onDeleteRoutePointClick
                )

                1 -> MapTab(
                    tripId = tripId,
                    routePoints = routeUiState.routePoints
                        .filter { point ->
                            point.dayNumber == routeUiState.selectedDayNumber
                        }
                        .sortedBy { point ->
                            point.order
                        },
                )

                2 -> BudgetTab(
                    tripId = tripId,
                    uiState = budgetUiState,
                    participants = participantsUiState.participants,
                    canEdit = participantsUiState.canEditTrip,
                    onTitleChange = onBudgetTitleChange,
                    onCategoryChange = onBudgetCategoryChange,
                    onAmountChange = onBudgetAmountChange,
                    onExpenseDateChange = onBudgetExpenseDateChange,
                    onOwnerTypeChange = onBudgetOwnerTypeChange,
                    onOwnerUserChange = onBudgetOwnerUserChange,
                    onSortTypeChange = onBudgetSortTypeChange,
                    onBudgetLimitInputChange = onBudgetLimitInputChange,
                    onSaveBudgetLimitClick = onSaveBudgetLimitClick,
                    onAddExpenseClick = onAddExpenseClick,
                    onExpenseAddedHandled = onExpenseAddedHandled,
                    onUpdateExpenseClick = onUpdateExpenseClick,
                    onDeleteExpenseClick = onDeleteExpenseClick
                )

                3 -> ParticipantsTab(
                    tripId = tripId,
                    uiState = participantsUiState,
                    currentUserRole = participantsUiState.currentUserRole,
                    canInvite = participantsUiState.canInviteParticipants,
                    onEmailChange = onParticipantEmailChange,
                    onRoleChange = onParticipantRoleChange,
                    onInviteClick = onInviteParticipantClick,
                    onUpdateRoleClick = onUpdateParticipantRoleClick,
                    onDeleteParticipantClick = onDeleteParticipantClick
                )
            }
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog.value = false
            },
            title = {
                Text("Удалить поездку?")
            },
            text = {
                Text(
                    text = tripErrorMessage
                        ?: "Это действие удалит поездку, маршрут, расходы, участников, приглашения и уведомления."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        onDeleteTripClick()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}


/**
 * Новый переключатель вкладок.
 */
@Composable
private fun TripTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            TripTabChip(
                title = title,
                isSelected = selectedIndex == index,
                onClick = {
                    onTabClick(index)
                }
            )
        }
    }
}

/**
 * Одна вкладка-плашка.
 */
@Composable
private fun TripTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) {
        Color(0xFF2563EB)
    } else {
        Color.White
    }

    val content = if (isSelected) {
        Color.White
    } else {
        Color(0xFF111827)
    }

    val border = if (isSelected) {
        Color(0xFF2563EB)
    } else {
        Color(0xFFE5E7EB)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = background,
        border = BorderStroke(
            width = 1.dp,
            color = border
        )
    ) {
        Text(
            text = title,
            color = content,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 9.dp
            )
        )
    }
}

private fun calculateTripDaysCount(
    startDate: String,
    endDate: String
): Int {
    if (startDate.isBlank() || endDate.isBlank()) {
        return 1
    }

    return try {
        val formatter = java.text.SimpleDateFormat(
            "dd.MM.yyyy",
            java.util.Locale.getDefault()
        )

        val start = formatter.parse(startDate)
        val end = formatter.parse(endDate)

        if (start == null || end == null) {
            1
        } else {
            val diffMillis = end.time - start.time
            val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
            days.coerceAtLeast(1)
        }
    } catch (exception: Exception) {
        1
    }
}

@Preview(showBackground = true)
@Composable
private fun TripScreenPreview() {
    TravelAppTheme {
        TripScreen(
            tripId = "trip-1",
            onBackClick = {},
            tripTitle = "Питер",
            tripStartDate = "12.05.2026",
            tripEndDate = "15.05.2026",
            onRouteSelectedDayChange = {},
            isDeletingTrip = false,
            tripErrorMessage = null,
            canDeleteTrip = true,
            onDeleteTripClick = {},
            routeUiState = RouteUiState(),
            onRouteSearchQueryChange = {},
            onRouteSearchClick = {},
            onRoutePlaceClick = {},
            onRouteDescriptionChange = {},
            onAddSelectedPlaceClick = {},
            onReorderRoutePoints = {},
            onDeleteRoutePointClick = {},
            budgetUiState = BudgetUiState(),
            onBudgetTitleChange = {},
            onBudgetCategoryChange = {},
            onBudgetAmountChange = {},
            onBudgetExpenseDateChange = {},
            onBudgetOwnerTypeChange = {},
            onBudgetOwnerUserChange = { _, _ -> },
            onBudgetSortTypeChange = {},
            onBudgetLimitInputChange = {},
            onSaveBudgetLimitClick = {},
            onUpdateExpenseClick = { _, _, _, _, _, _, _, _ -> },
            onAddExpenseClick = {},
            onExpenseAddedHandled = {},
            onDeleteExpenseClick = {},
            participantsUiState = ParticipantsUiState(
                currentUserRole = ParticipantRole.ORGANIZER,
                canEditTrip = true,
                canInviteParticipants = true
            ),
            onParticipantEmailChange = {},
            onParticipantRoleChange = {},
            onInviteParticipantClick = {},
            onEditRoutePointClick = { _, _, _ -> },
            onRoutePointAddedHandled = {},
            onUpdateParticipantRoleClick = { _, _ -> },
            onDeleteParticipantClick = { _, _ -> }
        )
    }
}