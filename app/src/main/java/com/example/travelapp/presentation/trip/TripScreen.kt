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
    onMoveRoutePointUpClick: (String) -> Unit,
    onMoveRoutePointDownClick: (String) -> Unit,
    onDeleteRoutePointClick: (String) -> Unit,

    budgetUiState: BudgetUiState,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetCategoryChange: (ExpenseCategory) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseAddedHandled: () -> Unit,
    onDeleteExpenseClick: (String) -> Unit,

    participantsUiState: ParticipantsUiState,
    onParticipantEmailChange: (String) -> Unit,
    onParticipantRoleChange: (String) -> Unit,
    onInviteParticipantClick: () -> Unit
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
                    canEdit = participantsUiState.canEditTrip,
                    onSearchQueryChange = onRouteSearchQueryChange,
                    onSearchClick = onRouteSearchClick,
                    onPlaceClick = onRoutePlaceClick,
                    onDescriptionChange = onRouteDescriptionChange,
                    onAddSelectedPlaceClick = onAddSelectedPlaceClick,
                    onMovePointUpClick = onMoveRoutePointUpClick,
                    onMovePointDownClick = onMoveRoutePointDownClick,
                    onDeletePointClick = onDeleteRoutePointClick
                )

                1 -> MapTab(
                    tripId = tripId,
                    routePoints = routeUiState.routePoints
                )

                2 -> BudgetTab(
                    tripId = tripId,
                    uiState = budgetUiState,
                    canEdit = participantsUiState.canEditTrip,
                    onTitleChange = onBudgetTitleChange,
                    onCategoryChange = onBudgetCategoryChange,
                    onAmountChange = onBudgetAmountChange,
                    onAddExpenseClick = onAddExpenseClick,
                    onExpenseAddedHandled = onExpenseAddedHandled,
                    onDeleteExpenseClick = onDeleteExpenseClick
                )

                3 -> ParticipantsTab(
                    tripId = tripId,
                    uiState = participantsUiState,
                    currentUserRole = participantsUiState.currentUserRole,
                    canInvite = participantsUiState.canInviteParticipants,
                    onEmailChange = onParticipantEmailChange,
                    onRoleChange = onParticipantRoleChange,
                    onInviteClick = onInviteParticipantClick
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

@Preview(showBackground = true)
@Composable
private fun TripScreenPreview() {
    TravelAppTheme {
        TripScreen(
            tripId = "trip-1",
            onBackClick = {},
            tripTitle = "Питер",
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
            onMoveRoutePointUpClick = {},
            onMoveRoutePointDownClick = {},
            onDeleteRoutePointClick = {},
            budgetUiState = BudgetUiState(),
            onBudgetTitleChange = {},
            onBudgetCategoryChange = {},
            onBudgetAmountChange = {},
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
            onInviteParticipantClick = {}
        )
    }
}