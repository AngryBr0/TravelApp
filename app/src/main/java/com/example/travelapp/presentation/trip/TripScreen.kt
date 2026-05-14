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
import com.example.travelapp.presentation.participants.ParticipantsUiState
import com.example.travelapp.presentation.route.RouteTab
import com.example.travelapp.presentation.route.RouteUiState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.example.travelapp.data.model.PlaceSearchResult
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
    onDeleteRoutePointClick: (String) -> Unit,

    budgetUiState: BudgetUiState,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetCategoryChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteExpenseClick: (String) -> Unit,

    participantsUiState: ParticipantsUiState,
    onParticipantEmailChange: (String) -> Unit,
    onParticipantRoleChange: (String) -> Unit,
    onInviteParticipantClick: () -> Unit
) {
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    /**
     * Управляет показом диалога подтверждения удаления.
     */
    var showDeleteDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Маршрут",
        "Карта",
        "Бюджет",
        "Участники"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Назад")
        }
        Text(
            text = tripTitle.ifBlank { "Поездка" },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (canDeleteTrip) {
            Button(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDeletingTrip
            ) {
                Text("Удалить поездку")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isDeletingTrip) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (tripErrorMessage != null) {
            Text(text = tripErrorMessage)
            Spacer(modifier = Modifier.height(8.dp))
        }
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
                canEdit = participantsUiState.canEditTrip,
                onSearchQueryChange = onRouteSearchQueryChange,
                onSearchClick = onRouteSearchClick,
                onPlaceClick = onRoutePlaceClick,
                onDescriptionChange = onRouteDescriptionChange,
                onAddSelectedPlaceClick = onAddSelectedPlaceClick,
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
    /**
     * Диалог подтверждения удаления.
     *
     * Он нужен, чтобы пользователь случайно не удалил поездку.
     */
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Удалить поездку?")
            },
            text = {
                Text("Это действие удалит поездку, маршрут, расходы, участников, приглашения и уведомления.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteTripClick()
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}
