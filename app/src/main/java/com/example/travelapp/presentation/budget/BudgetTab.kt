package com.example.travelapp.presentation.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.ui.components.AppCard
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.zIndex

/**
 * BudgetTab — вкладка бюджета поездки.
 *
 * Список расходов отображается компактно.
 * Кнопка добавления расхода находится снизу по центру,
 * как плавающее основное действие.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTab(
    tripId: String,
    uiState: BudgetUiState,
    canEdit: Boolean,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseAddedHandled: () -> Unit,
    onDeleteExpenseClick: (String) -> Unit
) {
    val isAddExpenseSheetVisible = remember {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(uiState.isExpenseAdded) {
        if (uiState.isExpenseAdded) {
            isAddExpenseSheetVisible.value = false
            onExpenseAddedHandled()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                BudgetSummaryCard(
                    totalAmount = uiState.totalAmount
                )
            }

            if (!canEdit) {
                item {
                    AppEmptyState(
                        text = "У вас режим просмотра. Редактирование бюджета недоступно."
                    )
                }
            }

            item {
                AppSectionTitle(text = "Расходы")
            }

            if (uiState.expenses.isEmpty()) {
                item {
                    AppEmptyState(text = "Пока расходы не добавлены.")
                }
            } else {
                items(uiState.expenses) { expense ->
                    ExpenseRow(
                        expense = expense,
                        canDelete = canEdit,
                        onDeleteClick = {
                            onDeleteExpenseClick(expense.id)
                        }
                    )
                }
            }
        }

        if (canEdit) {
            Button(
                onClick = {
                    isAddExpenseSheetVisible.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .height(42.dp)
                    .width(190.dp)
                    .zIndex(1f),
                shape = RoundedCornerShape(21.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "+ Добавить расход",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }

    if (isAddExpenseSheetVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isAddExpenseSheetVisible.value = false
            },
            sheetState = sheetState
        ) {
            AddExpenseSheetContent(
                uiState = uiState,
                onTitleChange = onTitleChange,
                onCategoryChange = onCategoryChange,
                onAmountChange = onAmountChange,
                onAddExpenseClick = onAddExpenseClick
            )
        }
    }
}

/**
 * Карточка с общей суммой бюджета.
 */
@Composable
private fun BudgetSummaryCard(
    totalAmount: Double
) {
    AppCard {
        AppSectionTitle(text = "Бюджет поездки")

        Text(
            text = "${totalAmount} ₽",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        AppMutedText(
            text = "Общая сумма всех расходов."
        )
    }
}

/**
 * Содержимое нижнего окна добавления расхода.
 */
@Composable
private fun AddExpenseSheetContent(
    uiState: BudgetUiState,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionTitle(text = "Новый расход")

        AppMutedText(
            text = "Укажите сумму и категорию. Описание можно оставить пустым."
        )

        AppTextField(
            value = uiState.amount,
            onValueChange = onAmountChange,
            label = "Сумма",
            placeholder = "Например: 5000"
        )

        ExpenseCategoryDropdown(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategoryChange
        )

        AppTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = "Описание",
            placeholder = "Необязательно",
            singleLine = false,
            maxLines = 2
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            AppErrorMessage(message = uiState.errorMessage)
        }

        Button(
            onClick = onAddExpenseClick,
            enabled = !uiState.isLoading,
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
                text = "Добавить",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Выпадающий список категорий расходов.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCategoryDropdown(
    selectedCategory: ExpenseCategory,
    onCategorySelected: (ExpenseCategory) -> Unit
) {
    val expanded = remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            expanded.value = !expanded.value
        }
    ) {
        OutlinedTextField(
            value = selectedCategory.displayName,
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Категория")
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
            ExpenseCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(category.displayName)
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Компактная строка расхода.
 *
 * Специально не используем AppCard, потому что AppCard слишком высокий
 * для списка расходов. Здесь меньше padding и более плотная верстка.
 */
@Composable
private fun ExpenseRow(
    expense: Expense,
    canDelete: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE5E7EB)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = expense.title.ifBlank { expense.category.displayName },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                if (expense.title.isNotBlank()) {
                    Text(
                        text = expense.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = "${expense.amount} ₽",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            if (canDelete) {
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить расход",
                        tint = MaterialTheme.colorScheme.error
                    )
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
                totalAmount = 18200.0,
                title = "Отель на 3 ночи",
                selectedCategory = ExpenseCategory.HOTEL,
                amount = "12000",
                expenses = listOf(
                    Expense(
                        id = "1",
                        tripId = "trip-1",
                        title = "Отель",
                        category = ExpenseCategory.HOTEL,
                        amount = 12000.0,
                        userId = "user-1",
                        date = "12.05.2026"
                    ),
                    Expense(
                        id = "2",
                        tripId = "trip-1",
                        title = "Такси до вокзала",
                        category = ExpenseCategory.TAXI,
                        amount = 1200.0,
                        userId = "user-1",
                        date = "12.05.2026"
                    ),
                    Expense(
                        id = "3",
                        tripId = "trip-1",
                        title = "Ужин",
                        category = ExpenseCategory.CAFE,
                        amount = 5000.0,
                        userId = "user-1",
                        date = "12.05.2026"
                    )
                )
            ),
            canEdit = true,
            onTitleChange = {},
            onCategoryChange = {},
            onAmountChange = {},
            onAddExpenseClick = {},
            onDeleteExpenseClick = {},
            onExpenseAddedHandled = {}
        )
    }
}