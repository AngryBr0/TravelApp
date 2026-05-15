package com.example.travelapp.presentation.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppPrimaryButton
import com.example.travelapp.ui.components.AppScaffold
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.ui.components.AppDateField
/**
 * CreateTripScreen — экран создания поездки.
 */
@Composable
fun CreateTripScreen(
    uiState: CreateTripUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onBackClick: () -> Unit,
    onTripCreated: () -> Unit
) {
    LaunchedEffect(uiState.isCreated) {
        if (uiState.isCreated) {
            onTripCreated()
        }
    }

    AppScaffold(
        title = "Новая поездка",
        onBackClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppCard {
                    AppSectionTitle(text = "Основная информация")

                    AppMutedText(
                        text = "Заполните данные поездки. Потом можно будет добавить маршрут, участников и расходы."
                    )

                    AppTextField(
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        label = "Название",
                        placeholder = "Например: Казань — сентябрь"
                    )

                    AppTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = "Описание",
                        placeholder = "Короткое описание поездки",
                        singleLine = false,
                        maxLines = 3
                    )

                    AppDateField(
                        value = uiState.startDate,
                        onDateSelected = onStartDateChange,
                        label = "Дата начала",
                        placeholder = "Выберите дату начала"
                    )

                    AppDateField(
                        value = uiState.endDate,
                        onDateSelected = onEndDateChange,
                        label = "Дата окончания",
                        placeholder = "Выберите дату окончания"
                    )

                    AppErrorMessage(message = uiState.errorMessage)

                    AppPrimaryButton(
                        text = "Создать поездку",
                        onClick = onCreateClick,
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

@Preview(showBackground = true)
@Composable
private fun CreateTripScreenPreview() {
    TravelAppTheme {
        CreateTripScreen(
            uiState = CreateTripUiState(
                title = "Казань — сентябрь",
                description = "Маршрут на несколько дней",
                startDate = "5 сент 2026",
                endDate = "8 сент 2026"
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onCreateClick = {},
            onBackClick = {},
            onTripCreated = {}
        )
    }
}