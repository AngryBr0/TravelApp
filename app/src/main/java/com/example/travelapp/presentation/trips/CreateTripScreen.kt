package com.example.travelapp.presentation.trips

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import com.example.travelapp.ui.components.ErrorMessage
import com.example.travelapp.ui.components.PrimaryButton
import com.example.travelapp.ui.components.TravelCard
import com.example.travelapp.ui.components.TravelScreen
import com.example.travelapp.ui.components.TravelTextField

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

    TravelScreen(
        title = "Новая поездка",
        onBackClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TravelCard {
                    Text(
                        text = "Основная информация",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    TravelTextField(
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        label = "Название поездки"
                    )

                    TravelTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = "Описание",
                        singleLine = false
                    )

                    TravelTextField(
                        value = uiState.startDate,
                        onValueChange = onStartDateChange,
                        label = "Дата начала"
                    )

                    TravelTextField(
                        value = uiState.endDate,
                        onValueChange = onEndDateChange,
                        label = "Дата окончания"
                    )

                    ErrorMessage(message = uiState.errorMessage)

                    PrimaryButton(
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