package com.example.travelapp.presentation.budget

import androidx.compose.ui.tooling.preview.Preview
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Expense

/**
 * BudgetTab — вкладка бюджета поездки.
 *
 * Вкладка показывает:
 * - форму добавления расхода;
 * - общую сумму;
 * - список расходов;
 * - кнопку удаления расхода.
 */
@Composable
fun BudgetTab(
    tripId: String,
    uiState: BudgetUiState,
    canEdit: Boolean,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteExpenseClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Бюджет поездки")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Общая сумма: ${uiState.totalAmount} ₽")

        Spacer(modifier = Modifier.height(16.dp))

        if (canEdit) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("Название расхода") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.category,
            onValueChange = onCategoryChange,
            label = { Text("Категория: транспорт, еда, отель...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = onAmountChange,
            label = { Text("Сумма") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onAddExpenseClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Добавить расход")
        }
        } else {
            Text(text = "У вас режим просмотра. Редактирование бюджета недоступно.")
        }
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Список расходов")

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.expenses.isEmpty()) {
            Text(text = "Пока расходы не добавлены")
        } else {
            LazyColumn {
                items(uiState.expenses) { expense ->
                    ExpenseCard(
                        expense = expense,
                        canDelete = canEdit,
                        onDeleteClick = {
                            onDeleteExpenseClick(expense.id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Карточка одного расхода.
 */
@Composable
private fun ExpenseCard(
    expense: Expense,
    canDelete: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = expense.title)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Категория: ${expense.category}")

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Сумма: ${expense.amount} ₽")

            if (canDelete) {
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onDeleteClick) {
                    Text("Удалить")
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun BudgetTabPreview() {
    TravelAppTheme {
        BudgetTab(
            tripId = "trip-1",
            uiState = BudgetUiState(
                totalAmount = 6200.0,
                expenses = listOf(
                    Expense(
                        id = "1",
                        title = "Отель",
                        amount = 5000.0
                    ),
                    Expense(
                        id = "2",
                        title = "Такси",
                        amount = 1200.0
                    )
                )
            ),
            onTitleChange = {},
            onCategoryChange = {},
            onAmountChange = {},
            onAddExpenseClick = {},
            onDeleteExpenseClick = {},
            canEdit = true
        )
    }
}