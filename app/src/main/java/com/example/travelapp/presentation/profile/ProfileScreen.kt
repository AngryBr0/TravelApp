package com.example.travelapp.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppDangerButton
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppPrimaryButton
import com.example.travelapp.ui.components.AppSecondaryButton
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.components.MainTabScaffold
import com.example.travelapp.ui.components.TripsBottomItem
import com.example.travelapp.ui.theme.TravelAppTheme

/**
 * ProfileScreen — экран профиля пользователя.
 */
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onTripsClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onEditClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onSaveProfileClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSignOutClick: () -> Unit,
    onSignedOut: () -> Unit
) {
    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onSignedOut()
        }
    }

    MainTabScaffold(
        title = "Профиль",
        selectedItem = TripsBottomItem.PROFILE,
        onTripsClick = onTripsClick,
        onInvitationsClick = onInvitationsClick,
        onNotificationsClick = onNotificationsClick,
        onProfileClick = onProfileClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppSectionTitle(text = "Данные аккаунта")
            }

            item {
                AppCard {
                    if (uiState.isEditing) {
                        AppTextField(
                            value = uiState.editableName,
                            onValueChange = onNameChange,
                            label = "Имя",
                            placeholder = "Введите имя"
                        )

                        AppTextField(
                            value = uiState.editableEmail,
                            onValueChange = onEmailChange,
                            label = "Email",
                            placeholder = "Введите email"
                        )

                        AppErrorMessage(message = uiState.errorMessage)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppPrimaryButton(
                                text = "Сохранить",
                                onClick = onSaveProfileClick,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving
                            )

                            AppSecondaryButton(
                                text = "Отмена",
                                onClick = onCancelEditClick,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving
                            )
                        }

                        if (uiState.isSaving) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ProfileRow(
                            label = "Имя",
                            value = uiState.name.ifBlank { "Не указано" }
                        )

                        ProfileRow(
                            label = "Email",
                            value = uiState.email.ifBlank { "Не указан" }
                        )

                        ProfileRow(
                            label = "ID пользователя",
                            value = uiState.userId.ifBlank { "Неизвестно" }
                        )

                        AppSecondaryButton(
                            text = "Редактировать профиль",
                            onClick = onEditClick
                        )
                    }
                }
            }

            item {
                AppSectionTitle(text = "Аккаунт")
            }

            item {
                AppCard {
                    AppMutedText(
                        text = "После выхода вы вернётесь на экран входа."
                    )

                    AppErrorMessage(message = uiState.errorMessage)

                    AppDangerButton(
                        text = "Выйти из аккаунта",
                        onClick = onSignOutClick,
                        enabled = !uiState.isLoading
                    )

                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    TravelAppTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                userId = "user-123",
                name = "Денис",
                email = "denis@mail.ru",
                editableName = "Денис",
                editableEmail = "denis@mail.ru"
            ),
            onTripsClick = {},
            onInvitationsClick = {},
            onNotificationsClick = {},
            onProfileClick = {},
            onEditClick = {},
            onCancelEditClick = {},
            onSaveProfileClick = {},
            onNameChange = {},
            onEmailChange = {},
            onSignOutClick = {},
            onSignedOut = {}
        )
    }
}