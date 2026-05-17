package com.example.travelapp.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 *
 * ID пользователя скрыт в техническом блоке,
 * потому что это не основная информация для пользователя.
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
                ProfileHeaderCard(
                    name = uiState.name,
                    email = uiState.email
                )
            }

            item {
                AppSectionTitle(text = "Данные аккаунта")
            }

            item {
                AccountDataCard(
                    uiState = uiState,
                    onEditClick = onEditClick,
                    onCancelEditClick = onCancelEditClick,
                    onSaveProfileClick = onSaveProfileClick,
                    onNameChange = onNameChange,
                    onEmailChange = onEmailChange
                )
            }

            item {
                TechnicalInfoCard(
                    userId = uiState.userId
                )
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

/**
 * Верхняя карточка профиля с аватаркой, именем и email.
 */
@Composable
private fun ProfileHeaderCard(
    name: String,
    email: String
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileAvatar(
                name = name,
                email = email
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = name.ifBlank { "Пользователь" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = email.ifBlank { "Email не указан" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

            }
        }
    }
}

/**
 * Круглая аватарка с инициалами пользователя.
 */
@Composable
private fun ProfileAvatar(
    name: String,
    email: String
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(50)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getInitials(
                name = name,
                email = email
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB)
        )
    }
}

/**
 * Карточка данных аккаунта.
 */
@Composable
private fun AccountDataCard(
    uiState: ProfileUiState,
    onEditClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onSaveProfileClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit
) {
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

            AppSecondaryButton(
                text = "Редактировать профиль",
                onClick = onEditClick
            )
        }
    }
}

/**
 * Скрытая техническая информация.
 *
 * ID пользователя не показываем на основном экране,
 * потому что это технический идентификатор.
 */
@Composable
private fun TechnicalInfoCard(
    userId: String
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }

    AppCard(
        modifier = Modifier.clickable {
            isExpanded = !isExpanded
        }
    ) {
        Text(
            text = "Техническая информация",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AppMutedText(
            text = if (isExpanded) {
                "ID пользователя отображается ниже."
            } else {
                "Нажмите, чтобы показать ID пользователя."
            }
        )

        if (isExpanded) {
            ProfileRow(
                label = "ID пользователя",
                value = userId.ifBlank { "Неизвестно" }
            )
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
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
}

/**
 * Получает инициалы для аватарки.
 */
private fun getInitials(
    name: String,
    email: String
): String {
    val source = name
        .ifBlank {
            email.substringBefore("@")
        }
        .trim()

    if (source.isBlank()) {
        return "U"
    }

    val parts = source
        .split(" ")
        .filter { part ->
            part.isNotBlank()
        }

    return when {
        parts.size >= 2 -> {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        }

        parts.size == 1 -> {
            parts[0].take(1).uppercase()
        }

        else -> {
            "U"
        }
    }
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