package com.example.travelapp.presentation.participants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.ParticipantStatus
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.ui.components.AppBottomActionButton
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppPrimaryButton
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.ui.components.AppDangerButton

/**
 * ParticipantsTab — вкладка участников поездки.
 *
 * Здесь пользователь видит список участников и свою роль.
 *
 * Организатор может:
 * - приглашать новых участников;
 * - менять роли участников.
 *
 * Роль организатора менять нельзя.
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
    onInviteClick: () -> Unit,
    onUpdateRoleClick: (String, ParticipantRole) -> Unit,
    onDeleteParticipantClick: (String, String) -> Unit
) {
    val isInviteSheetVisible = remember {
        mutableStateOf(false)
    }

    var editingParticipant by remember {
        mutableStateOf<TripParticipant?>(null)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val canChangeRoles = currentUserRole == ParticipantRole.ORGANIZER

    val acceptedParticipants = uiState.participants.filter { participant ->
        participant.status == ParticipantStatus.ACCEPTED
    }

    val invitedParticipants = uiState.participants.filter { participant ->
        participant.status == ParticipantStatus.INVITED
    }

    val declinedParticipants = uiState.participants.filter { participant ->
        participant.status == ParticipantStatus.DECLINED
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CurrentRoleCard(
                    currentUserRole = currentUserRole,
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage
                )
            }

            item {
                AppSectionTitle(text = "Участники")
            }

            if (acceptedParticipants.isEmpty()) {
                item {
                    AppEmptyState(text = "Пока нет принятых участников.")
                }
            } else {
                items(acceptedParticipants) { participant ->
                    val canManageParticipant = canChangeRoles &&
                            participant.role != ParticipantRole.ORGANIZER

                    val canEditRole = canManageParticipant &&
                            participant.status == ParticipantStatus.ACCEPTED

                    ParticipantRow(
                        participant = participant,
                        canManageParticipant = canManageParticipant,
                        canEditRole = canEditRole,
                        onClick = {
                            if (canManageParticipant) {
                                editingParticipant = participant
                            }
                        }
                    )
                }
            }

            if (canChangeRoles && invitedParticipants.isNotEmpty()) {
                item {
                    AppSectionTitle(text = "Ожидают ответа")
                }

                items(invitedParticipants) { participant ->
                    val canManageParticipant = canChangeRoles &&
                            participant.role != ParticipantRole.ORGANIZER

                    ParticipantRow(
                        participant = participant,
                        canManageParticipant = canManageParticipant,
                        canEditRole = false,
                        onClick = {
                            if (canManageParticipant) {
                                editingParticipant = participant
                            }
                        }
                    )
                }
            }

            if (canChangeRoles && declinedParticipants.isNotEmpty()) {
                item {
                    AppSectionTitle(text = "Отклонили приглашение")
                }

                items(declinedParticipants) { participant ->
                    val canManageParticipant = canChangeRoles &&
                            participant.role != ParticipantRole.ORGANIZER

                    ParticipantRow(
                        participant = participant,
                        canManageParticipant = canManageParticipant,
                        canEditRole = false,
                        onClick = {
                            if (canManageParticipant) {
                                editingParticipant = participant
                            }
                        }
                    )
                }
            }
        }

        if (canInvite) {
            AppBottomActionButton(
                text = "Пригласить",
                icon = Icons.Filled.GroupAdd,
                onClick = {
                    isInviteSheetVisible.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                width = 220.dp
            )
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
                onInviteClick = onInviteClick
            )
        }
    }

    if (editingParticipant != null) {
        ModalBottomSheet(
            onDismissRequest = {
                editingParticipant = null
            },
            sheetState = sheetState
        ) {
            EditParticipantRoleSheetContent(
                participant = editingParticipant!!,
                isLoading = uiState.isLoading,
                onSaveClick = { newRole ->
                    onUpdateRoleClick(
                        editingParticipant!!.id,
                        newRole
                    )

                    editingParticipant = null
                },
                onDeleteClick = {
                    onDeleteParticipantClick(
                        editingParticipant!!.id,
                        editingParticipant!!.email
                    )

                    editingParticipant = null
                }
            )
        }
    }
}

/**
 * Карточка текущей роли пользователя.
 */
@Composable
private fun CurrentRoleCard(
    currentUserRole: ParticipantRole,
    isLoading: Boolean,
    errorMessage: String?
) {
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

        if (isLoading) {
            CircularProgressIndicator()
        }

        AppErrorMessage(message = errorMessage)
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

        ParticipantRoleInviteDropdown(
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

//*Bottom sheet управления участником. */
@Composable
private fun EditParticipantRoleSheetContent(
    participant: TripParticipant,
    isLoading: Boolean,
    onSaveClick: (ParticipantRole) -> Unit,
    onDeleteClick: () -> Unit
) {
    var selectedRole by remember(participant.id) {
        mutableStateOf(
            if (participant.role == ParticipantRole.ORGANIZER) {
                ParticipantRole.VIEWER
            } else {
                participant.role
            }
        )
    }

    val canEditRole = participant.status == ParticipantStatus.ACCEPTED

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
        AppSectionTitle(text = "Управление участником")

        Text(
            text = participantDisplayName(participant),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AppMutedText(
            text = participant.email.ifBlank { "Email не указан" }
        )

        AppMutedText(
            text = "Статус: ${participantStatusText(participant.status)}"
        )

        if (canEditRole) {
            ParticipantRoleEditDropdown(
                selectedRole = selectedRole,
                onRoleSelected = {
                    selectedRole = it
                }
            )

            Button(
                onClick = {
                    onSaveClick(selectedRole)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Сохранить роль",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            AppMutedText(
                text = "Роль можно менять только у участников, которые приняли приглашение."
            )
        }

        AppDangerButton(
            text = "Удалить участника",
            onClick = onDeleteClick,
            enabled = !isLoading
        )

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

private fun participantStatusText(
    status: ParticipantStatus
): String {
    return when (status) {
        ParticipantStatus.ACCEPTED -> "Принят"
        ParticipantStatus.INVITED -> "Ожидает ответа"
        ParticipantStatus.DECLINED -> "Отклонил приглашение"
    }
}

/**
 * Выпадающий список ролей при приглашении.
 *
 * ORGANIZER не даём выбрать при приглашении,
 * потому что организатором является создатель поездки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantRoleInviteDropdown(
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
 * Выпадающий список ролей при редактировании участника.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantRoleEditDropdown(
    selectedRole: ParticipantRole,
    onRoleSelected: (ParticipantRole) -> Unit
) {
    val expanded = remember {
        mutableStateOf(false)
    }

    val roles = listOf(
        ParticipantRole.EDITOR,
        ParticipantRole.VIEWER
    )

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
                        onRoleSelected(role)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Строка участника.
 *
 * canManageParticipant отвечает за то, можно ли открыть окно управления.
 * canEditRole отвечает только за возможность менять роль.
 *
 * Поэтому INVITED и DECLINED можно открыть и удалить,
 * но нельзя менять им роль.
 */
@Composable
private fun ParticipantRow(
    participant: TripParticipant,
    canManageParticipant: Boolean,
    canEditRole: Boolean,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.clickable(
            enabled = canManageParticipant,
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = participantDisplayName(participant),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                AppMutedText(
                    text = participantSubtitle(participant)
                )
            }

            ParticipantStatusPill(
                status = participant.status
            )
        }

        if (canManageParticipant) {
            AppMutedText(
                text = if (canEditRole) {
                    "Нажмите, чтобы изменить роль или удалить участника"
                } else {
                    "Нажмите, чтобы удалить запись"
                }
            )
        }
    }
}

/**
 * Подпись под именем участника.
 */
private fun participantSubtitle(
    participant: TripParticipant
): String {
    return if (participant.email.isNotBlank()) {
        "${participant.email} • ${roleText(participant.role)}"
    } else {
        roleText(participant.role)
    }
}

/**
 * Плашка статуса участника.
 */
@Composable
private fun ParticipantStatusPill(
    status: ParticipantStatus
) {
    val text = when (status) {
        ParticipantStatus.ACCEPTED -> "Принят"
        ParticipantStatus.INVITED -> "Ожидает"
        ParticipantStatus.DECLINED -> "Отклонён"
    }

    val color = when (status) {
        ParticipantStatus.ACCEPTED -> Color(0xFF15803D)
        ParticipantStatus.INVITED -> Color(0xFF2563EB)
        ParticipantStatus.DECLINED -> Color(0xFFDC2626)
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
private fun roleText(
    role: ParticipantRole
): String {
    return when (role) {
        ParticipantRole.ORGANIZER -> "Организатор"
        ParticipantRole.EDITOR -> "Редактор"
        ParticipantRole.VIEWER -> "Просмотр"
    }
}

/**
 * Возвращает имя участника для отображения в интерфейсе.
 */
private fun participantDisplayName(
    participant: TripParticipant
): String {
    return participant.name
        .ifBlank { participant.email }
        .ifBlank { "Участник" }
}

/**
 * Описание прав по роли.
 */
private fun roleDescription(
    role: ParticipantRole
): String {
    return when (role) {
        ParticipantRole.ORGANIZER -> {
            "Вы можете редактировать поездку, маршрут, бюджет, приглашать участников и менять роли."
        }

        ParticipantRole.EDITOR -> {
            "Вы можете редактировать маршрут и бюджет, но не можете приглашать участников и менять роли."
        }

        ParticipantRole.VIEWER -> {
            "Вы можете просматривать поездку без редактирования."
        }
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
                        name = "Денис",
                        role = ParticipantRole.ORGANIZER,
                        status = ParticipantStatus.ACCEPTED
                    ),
                    TripParticipant(
                        id = "2",
                        tripId = "trip-1",
                        email = "friend@mail.ru",
                        name = "Лёха",
                        role = ParticipantRole.EDITOR,
                        status = ParticipantStatus.ACCEPTED
                    ),
                    TripParticipant(
                        id = "3",
                        tripId = "trip-1",
                        email = "guest@mail.ru",
                        name = "",
                        role = ParticipantRole.VIEWER,
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
            onInviteClick = {},
            onUpdateRoleClick = { _, _ -> },
            onDeleteParticipantClick = { _, _ -> }
        )
    }
}