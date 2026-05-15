package com.example.travelapp.presentation.participants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.ParticipantStatus
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppPrimaryButton
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.foundation.layout.fillMaxSize

/**
 * ParticipantsTab — вкладка участников поездки.
 *
 * Здесь пользователь видит список участников и свою роль.
 * Организатор может пригласить нового участника через нижнее всплывающее окно.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsTab(
    tripId: String,
    uiState: ParticipantsUiState,
    currentUserRole: ParticipantRole,
    canInvite: Boolean,
    onEmailChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onInviteClick: () -> Unit
) {
    val isInviteSheetVisible = remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AppCard {
                    AppSectionTitle(text = "Участники поездки")

                    Text(
                        text = "Ваша роль: ${roleText(currentUserRole)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    AppMutedText(
                        text = roleDescription(currentUserRole)
                    )

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    }

                    AppErrorMessage(message = uiState.errorMessage)
                }
            }

            item {
                AppSectionTitle(text = "Список участников")
            }

            if (uiState.participants.isEmpty()) {
                item {
                    AppEmptyState(text = "Пока участники не добавлены.")
                }
            } else {
                items(uiState.participants) { participant ->
                    ParticipantRow(participant = participant)
                }
            }
        }

        if (canInvite) {
            Button(
                onClick = {
                    isInviteSheetVisible.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .height(42.dp)
                    .width(210.dp),
                shape = RoundedCornerShape(21.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.GroupAdd,
                    contentDescription = "Пригласить",
                    modifier = Modifier.padding(end = 6.dp)
                )

                Text(
                    text = "Пригласить",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }

    if (isInviteSheetVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isInviteSheetVisible.value = false
            },
            sheetState = sheetState
        ) {
            InviteParticipantSheetContent(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onRoleChange = onRoleChange,
                onInviteClick = {
                    onInviteClick()
                }
            )
        }
    }
}

/**
 * Содержимое bottom sheet для приглашения участника.
 */
@Composable
private fun InviteParticipantSheetContent(
    uiState: ParticipantsUiState,
    onEmailChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 28.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AppSectionTitle(text = "Пригласить участника")

        AppMutedText(
            text = "Введите email пользователя и выберите его роль в поездке."
        )

        AppTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "Например: friend@mail.ru"
        )

        ParticipantRoleDropdown(
            selectedRoleText = uiState.role,
            onRoleSelected = onRoleChange
        )

        AppErrorMessage(message = uiState.errorMessage)

        AppPrimaryButton(
            text = "Отправить приглашение",
            onClick = onInviteClick,
            enabled = !uiState.isLoading
        )

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Выпадающий список ролей.
 *
 * ORGANIZER не даём выбрать при приглашении,
 * потому что организатором является создатель поездки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantRoleDropdown(
    selectedRoleText: String,
    onRoleSelected: (String) -> Unit
) {
    val expanded = remember {
        mutableStateOf(false)
    }

    val roles = listOf(
        ParticipantRole.EDITOR,
        ParticipantRole.VIEWER
    )

    val selectedRole = roles.firstOrNull { role ->
        role.name.lowercase() == selectedRoleText.lowercase()
    } ?: ParticipantRole.VIEWER

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            expanded.value = !expanded.value
        }
    ) {
        OutlinedTextField(
            value = roleText(selectedRole),
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Роль")
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded.value
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = {
                expanded.value = false
            }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = {
                        Text(roleText(role))
                    },
                    onClick = {
                        onRoleSelected(role.name.lowercase())
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Компактная строка участника.
 */
@Composable
private fun ParticipantRow(
    participant: TripParticipant
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = participant.email.ifBlank { "Пользователь" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                AppMutedText(
                    text = roleText(participant.role)
                )
            }

            ParticipantStatusPill(
                status = participant.status
            )
        }
    }
}

/**
 * Плашка статуса участника.
 */
@Composable
private fun ParticipantStatusPill(
    status: ParticipantStatus
) {
    val text = when (status.name) {
        "ACCEPTED" -> "Принят"
        "INVITED" -> "Ожидает"
        "DECLINED" -> "Отклонён"
        else -> status.name
    }

    val color = when (status.name) {
        "ACCEPTED" -> Color(0xFF15803D)
        "INVITED" -> Color(0xFF2563EB)
        "DECLINED" -> Color(0xFFDC2626)
        else -> Color(0xFF6B7280)
    }

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold
    )
}

/**
 * Человекочитаемое название роли.
 */
private fun roleText(role: ParticipantRole): String {
    return when (role.name) {
        "ORGANIZER" -> "Организатор"
        "EDITOR" -> "Редактор"
        "VIEWER" -> "Просмотр"
        else -> role.name
    }
}

/**
 * Описание прав по роли.
 */
private fun roleDescription(role: ParticipantRole): String {
    return when (role.name) {
        "ORGANIZER" -> "Вы можете редактировать поездку, маршрут, бюджет и приглашать участников."
        "EDITOR" -> "Вы можете редактировать маршрут и бюджет, но не можете приглашать участников."
        "VIEWER" -> "Вы можете просматривать поездку без редактирования."
        else -> "Роль пользователя в поездке."
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantsTabPreview() {
    TravelAppTheme {
        ParticipantsTab(
            tripId = "trip-1",
            uiState = ParticipantsUiState(
                participants = listOf(
                    TripParticipant(
                        id = "1",
                        tripId = "trip-1",
                        email = "organizer@mail.ru",
                        role = ParticipantRole.ORGANIZER,
                        status = ParticipantStatus.ACCEPTED
                    ),
                    TripParticipant(
                        id = "2",
                        tripId = "trip-1",
                        email = "friend@mail.ru",
                        role = ParticipantRole.EDITOR,
                        status = ParticipantStatus.INVITED
                    )
                ),
                currentUserRole = ParticipantRole.ORGANIZER,
                canInviteParticipants = true,
                canEditTrip = true
            ),
            currentUserRole = ParticipantRole.ORGANIZER,
            canInvite = true,
            onEmailChange = {},
            onRoleChange = {},
            onInviteClick = {}
        )
    }
}