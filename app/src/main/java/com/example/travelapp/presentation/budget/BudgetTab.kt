package com.example.travelapp.presentation.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.model.ExpenseOwnerType
import com.example.travelapp.data.model.ParticipantRole
import com.example.travelapp.data.model.ParticipantStatus
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.ui.components.AppBottomActionButton
import com.example.travelapp.ui.components.AppDangerButton
import com.example.travelapp.ui.components.AppDateField
import com.example.travelapp.ui.components.AppEmptyState
import com.example.travelapp.ui.components.AppErrorMessage
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.components.AppSectionTitle
import com.example.travelapp.ui.components.AppTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton

/**
 * BudgetTab — вкладка бюджета поездки.
 *
 * Здесь реализованы:
 * - лимит бюджета поездки;
 * - сортировка расходов;
 * - дата расхода;
 * - общий / личный расход;
 * - выбор участника для личного расхода;
 * - редактирование и удаление расхода через bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTab(
    tripId: String,
    uiState: BudgetUiState,
    participants: List<TripParticipant>,
    canEdit: Boolean,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onExpenseDateChange: (String) -> Unit,
    onOwnerTypeChange: (ExpenseOwnerType) -> Unit,
    onOwnerUserChange: (String, String) -> Unit,
    onSortTypeChange: (ExpenseSortType) -> Unit,
    onBudgetLimitInputChange: (String) -> Unit,
    onSaveBudgetLimitClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onExpenseAddedHandled: () -> Unit,
    onUpdateExpenseClick: (
        expenseId: String,
        description: String,
        category: ExpenseCategory,
        amount: String,
        date: String,
        ownerType: ExpenseOwnerType,
        ownerUserId: String,
        ownerEmail: String
    ) -> Unit,
    onDeleteExpenseClick: (String) -> Unit
) {
    val isAddExpenseSheetVisible = remember {
        mutableStateOf(false)
    }

    val isBudgetLimitSheetVisible = remember {
        mutableStateOf(false)
    }

    var editingExpense by remember {
        mutableStateOf<Expense?>(null)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val sortedExpenses = remember(
        uiState.expenses,
        uiState.sortType
    ) {
        sortExpenses(
            expenses = uiState.expenses,
            sortType = uiState.sortType
        )
    }

    val acceptedParticipants = remember(participants) {
        participants.filter { participant ->
            participant.status == ParticipantStatus.ACCEPTED
        }
    }

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
                    totalAmount = uiState.totalAmount,
                    budgetLimit = uiState.budgetLimit,
                    canEdit = canEdit,
                    onEditBudgetClick = {
                        isBudgetLimitSheetVisible.value = true
                    }
                )
            }

            item {
                ExpenseSortDropdown(
                    selectedSortType = uiState.sortType,
                    onSortTypeSelected = onSortTypeChange
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

            if (sortedExpenses.isEmpty()) {
                item {
                    AppEmptyState(text = "Пока расходы не добавлены.")
                }
            } else {
                items(sortedExpenses) { expense ->
                    ExpenseRow(
                        expense = expense,
                        canEdit = canEdit,
                        onClick = {
                            if (canEdit) {
                                editingExpense = expense
                            }
                        }
                    )
                }
            }
        }

        if (canEdit) {
            AppBottomActionButton(
                text = "Добавить расход",
                icon = Icons.Filled.AccountBalanceWallet,
                onClick = {
                    isAddExpenseSheetVisible.value = true
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .zIndex(1f),
                width = 230.dp
            )
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
                participants = acceptedParticipants,
                onTitleChange = onTitleChange,
                onCategoryChange = onCategoryChange,
                onAmountChange = onAmountChange,
                onExpenseDateChange = onExpenseDateChange,
                onOwnerTypeChange = onOwnerTypeChange,
                onOwnerUserChange = onOwnerUserChange,
                onAddExpenseClick = onAddExpenseClick
            )
        }
    }
    if (isBudgetLimitSheetVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isBudgetLimitSheetVisible.value = false
            },
            sheetState = sheetState
        ) {
            BudgetLimitSheetContent(
                uiState = uiState,
                onBudgetLimitInputChange = onBudgetLimitInputChange,
                onSaveBudgetLimitClick = {
                    onSaveBudgetLimitClick()
                    isBudgetLimitSheetVisible.value = false
                }
            )
        }
    }

    if (editingExpense != null) {
        ModalBottomSheet(
            onDismissRequest = {
                editingExpense = null
            },
            sheetState = sheetState
        ) {
            EditExpenseSheetContent(
                expense = editingExpense!!,
                participants = acceptedParticipants,
                isLoading = uiState.isLoading,
                onSaveClick = { description, category, amount, date, ownerType, ownerUserId, ownerEmail ->
                    onUpdateExpenseClick(
                        editingExpense!!.id,
                        description,
                        category,
                        amount,
                        date,
                        ownerType,
                        ownerUserId,
                        ownerEmail
                    )

                    editingExpense = null
                },
                onDeleteClick = {
                    onDeleteExpenseClick(editingExpense!!.id)
                    editingExpense = null
                }
            )
        }
    }
}

/**
 * Компактная карточка бюджета.
 *
 * Поле редактирования лимита скрыто.
 * На экране показываем только основную информацию,
 * а изменение бюджета открывается через отдельное окно.
 */
@Composable
private fun BudgetSummaryCard(
    totalAmount: Double,
    budgetLimit: Double,
    canEdit: Boolean,
    onEditBudgetClick: () -> Unit
) {
    val remaining = budgetLimit - totalAmount
    val hasLimit = budgetLimit > 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppSectionTitle(text = "Бюджет поездки")

                    Text(
                        text = "${formatAmount(totalAmount)} ₽",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (canEdit) {
                    IconButton(
                        onClick = onEditBudgetClick
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Изменить бюджет"
                        )
                    }
                }
            }

            if (hasLimit) {
                Text(
                    text = "Лимит: ${formatAmount(budgetLimit)} ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (remaining >= 0) {
                        "Осталось: ${formatAmount(remaining)} ₽"
                    } else {
                        "Превышение: ${formatAmount(-remaining)} ₽"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (remaining >= 0) {
                        Color(0xFF15803D)
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                AppMutedText(
                    text = "Лимит бюджета не установлен."
                )
            }
        }
    }
}

/**
 * Bottom sheet для редактирования лимита бюджета поездки.
 */
@Composable
private fun BudgetLimitSheetContent(
    uiState: BudgetUiState,
    onBudgetLimitInputChange: (String) -> Unit,
    onSaveBudgetLimitClick: () -> Unit
) {
    var localError by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionTitle(text = "Лимит бюджета")

        AppMutedText(
            text = "Укажите сумму, которую планируется потратить на поездку."
        )

        AppTextField(
            value = uiState.budgetLimitInput,
            onValueChange = {
                onBudgetLimitInputChange(it)
                localError = null
            },
            label = "Бюджет поездки",
            placeholder = "Например: 50000"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            AppErrorMessage(
                message = localError ?: uiState.errorMessage
            )
        }

        Button(
            onClick = {
                val budget = uiState.budgetLimitInput
                    .replace(",", ".")
                    .toDoubleOrNull()

                if (budget == null || budget < 0.0) {
                    localError = "Введите корректный бюджет"
                } else {
                    onSaveBudgetLimitClick()
                }
            },
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
                text = "Сохранить",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}
/**
 * Bottom sheet добавления расхода.
 */
@Composable
private fun AddExpenseSheetContent(
    uiState: BudgetUiState,
    participants: List<TripParticipant>,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (ExpenseCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onExpenseDateChange: (String) -> Unit,
    onOwnerTypeChange: (ExpenseOwnerType) -> Unit,
    onOwnerUserChange: (String, String) -> Unit,
    onAddExpenseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionTitle(text = "Новый расход")

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

        AppDateField(
            value = uiState.expenseDate,
            onDateSelected = onExpenseDateChange,
            label = "Дата расхода",
            placeholder = "Выберите дату"
        )

        ExpenseOwnerTypeDropdown(
            selectedOwnerType = uiState.selectedOwnerType,
            onOwnerTypeSelected = onOwnerTypeChange
        )

        if (uiState.selectedOwnerType == ExpenseOwnerType.PERSONAL) {
            ParticipantDropdown(
                participants = participants,
                selectedEmail = uiState.selectedOwnerEmail,
                onParticipantSelected = onOwnerUserChange
            )
        }

        AppTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = "Описание",
            placeholder = "Необязательно",
            singleLine = false,
            maxLines = 3
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
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Bottom sheet редактирования расхода.
 */
@Composable
private fun EditExpenseSheetContent(
    expense: Expense,
    participants: List<TripParticipant>,
    isLoading: Boolean,
    onSaveClick: (
        description: String,
        category: ExpenseCategory,
        amount: String,
        date: String,
        ownerType: ExpenseOwnerType,
        ownerUserId: String,
        ownerEmail: String
    ) -> Unit,
    onDeleteClick: () -> Unit
) {
    var amount by remember(expense.id) {
        mutableStateOf(formatAmountInput(expense.amount))
    }

    var selectedCategory by remember(expense.id) {
        mutableStateOf(expense.category)
    }

    var date by remember(expense.id) {
        mutableStateOf(expense.date)
    }

    var ownerType by remember(expense.id) {
        mutableStateOf(expense.ownerType)
    }

    var ownerUserId by remember(expense.id) {
        mutableStateOf(expense.ownerUserId)
    }

    var ownerEmail by remember(expense.id) {
        mutableStateOf(expense.ownerEmail)
    }

    var description by remember(expense.id) {
        mutableStateOf(expense.title)
    }

    var localError by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 18.dp,
                end = 18.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionTitle(text = "Редактировать расход")

        AppTextField(
            value = amount,
            onValueChange = {
                amount = it
                localError = null
            },
            label = "Сумма",
            placeholder = "Например: 5000"
        )

        ExpenseCategoryDropdown(
            selectedCategory = selectedCategory,
            onCategorySelected = {
                selectedCategory = it
                localError = null
            }
        )

        AppDateField(
            value = date,
            onDateSelected = {
                date = it
                localError = null
            },
            label = "Дата расхода",
            placeholder = "Выберите дату"
        )

        ExpenseOwnerTypeDropdown(
            selectedOwnerType = ownerType,
            onOwnerTypeSelected = {
                ownerType = it
                localError = null

                if (it == ExpenseOwnerType.COMMON) {
                    ownerUserId = ""
                    ownerEmail = ""
                }
            }
        )

        if (ownerType == ExpenseOwnerType.PERSONAL) {
            ParticipantDropdown(
                participants = participants,
                selectedEmail = ownerEmail,
                onParticipantSelected = { id, email ->
                    ownerUserId = id
                    ownerEmail = email
                    localError = null
                }
            )
        }

        AppTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = "Описание",
            placeholder = "Необязательно",
            singleLine = false,
            maxLines = 3
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            AppErrorMessage(message = localError)
        }

        Button(
            onClick = {
                val parsedAmount = amount
                    .replace(",", ".")
                    .toDoubleOrNull()

                when {
                    parsedAmount == null || parsedAmount <= 0.0 -> {
                        localError = "Введите корректную сумму"
                    }

                    ownerType == ExpenseOwnerType.PERSONAL && ownerUserId.isBlank() -> {
                        localError = "Выберите участника"
                    }

                    else -> {
                        onSaveClick(
                            description,
                            selectedCategory,
                            amount,
                            date,
                            ownerType,
                            ownerUserId,
                            ownerEmail
                        )
                    }
                }
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
                text = "Сохранить",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        AppDangerButton(
            text = "Удалить расход",
            onClick = onDeleteClick,
            enabled = !isLoading
        )

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * Dropdown сортировки расходов.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseSortDropdown(
    selectedSortType: ExpenseSortType,
    onSortTypeSelected: (ExpenseSortType) -> Unit
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
            value = selectedSortType.displayName,
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Сортировка")
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
            ExpenseSortType.values().forEach { sortType ->
                DropdownMenuItem(
                    text = {
                        Text(sortType.displayName)
                    },
                    onClick = {
                        onSortTypeSelected(sortType)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Dropdown категорий расходов.
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
            leadingIcon = {
                Icon(
                    imageVector = expenseCategoryIcon(selectedCategory),
                    contentDescription = null
                )
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
                    leadingIcon = {
                        Icon(
                            imageVector = expenseCategoryIcon(category),
                            contentDescription = null
                        )
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
 * Dropdown типа расхода: общий / личный.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseOwnerTypeDropdown(
    selectedOwnerType: ExpenseOwnerType,
    onOwnerTypeSelected: (ExpenseOwnerType) -> Unit
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
            value = selectedOwnerType.displayName,
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Кому относится расход")
            },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedOwnerType == ExpenseOwnerType.COMMON) {
                        Icons.Filled.AccountBalanceWallet
                    } else {
                        Icons.Filled.Person
                    },
                    contentDescription = null
                )
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
            ExpenseOwnerType.values().forEach { ownerType ->
                DropdownMenuItem(
                    text = {
                        Text(ownerType.displayName)
                    },
                    onClick = {
                        onOwnerTypeSelected(ownerType)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/**
 * Dropdown выбора участника для личного расхода.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantDropdown(
    participants: List<TripParticipant>,
    selectedEmail: String,
    onParticipantSelected: (String, String) -> Unit
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
            value = selectedEmail.ifBlank {
                "Выберите участника"
            },
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Участник")
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
            participants.forEach { participant ->
                DropdownMenuItem(
                    text = {
                        Text(participantDisplayName(participant))
                    },
                    onClick = {
                        onParticipantSelected(
                            participant.id,
                            participantDisplayName(participant)
                        )
                        expanded.value = false
                    }
                )
            }
        }
    }
}

private fun participantDisplayName(
    participant: TripParticipant
): String {
    return participant.name
        .ifBlank { participant.email }
        .ifBlank { "Участник" }
}

/**
 * Строка расхода.
 */
@Composable
private fun ExpenseRow(
    expense: Expense,
    canEdit: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canEdit,
                onClick = onClick
            ),
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
                    vertical = 9.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExpenseCategoryIconBox(
                category = expense.category
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = expense.title.ifBlank {
                        expense.category.displayName
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = expenseSubtitle(expense),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${formatAmount(expense.amount)} ₽",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.widthIn(min = 48.dp)
            )
        }
    }
}

/**
 * Подпись под расходом.
 */
private fun expenseSubtitle(
    expense: Expense
): String {
    val ownerText = when (expense.ownerType) {
        ExpenseOwnerType.COMMON -> "Общий"
        ExpenseOwnerType.PERSONAL -> expense.ownerEmail.ifBlank { "Личный" }
    }

    return listOf(
        expense.category.displayName,
        expense.date,
        ownerText
    ).filter { value ->
        value.isNotBlank()
    }.joinToString(" • ")
}

/**
 * Иконка категории в строке расхода.
 */
@Composable
private fun ExpenseCategoryIconBox(
    category: ExpenseCategory
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF6FF)
        )
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = expenseCategoryIcon(category),
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Иконка категории.
 */
private fun expenseCategoryIcon(
    category: ExpenseCategory
): ImageVector {
    return when (category) {
        ExpenseCategory.TRANSPORT -> Icons.Filled.DirectionsCar
        ExpenseCategory.TAXI -> Icons.Filled.LocalTaxi
        ExpenseCategory.FLIGHTS -> Icons.Filled.Flight
        ExpenseCategory.TRAIN -> Icons.Filled.Train
        ExpenseCategory.FUEL -> Icons.Filled.LocalGasStation
        ExpenseCategory.PARKING -> Icons.Filled.LocalParking

        ExpenseCategory.FOOD -> Icons.Filled.Restaurant
        ExpenseCategory.CAFE -> Icons.Filled.LocalCafe
        ExpenseCategory.GROCERIES -> Icons.Filled.LocalGroceryStore

        ExpenseCategory.HOTEL -> Icons.Filled.Hotel
        ExpenseCategory.RENT -> Icons.Filled.Home

        ExpenseCategory.ENTERTAINMENT -> Icons.Filled.Paid
        ExpenseCategory.MUSEUMS -> Icons.Filled.Museum
        ExpenseCategory.EXCURSIONS -> Icons.Filled.Explore
        ExpenseCategory.TICKETS -> Icons.Filled.ConfirmationNumber

        ExpenseCategory.SHOPPING -> Icons.Filled.ShoppingBag
        ExpenseCategory.SOUVENIRS -> Icons.Filled.CardGiftcard

        ExpenseCategory.MEDICINE -> Icons.Filled.LocalPharmacy
        ExpenseCategory.INSURANCE -> Icons.Filled.HealthAndSafety
        ExpenseCategory.COMMUNICATION -> Icons.Filled.Wifi

        ExpenseCategory.OTHER -> Icons.Filled.Category
    }
}

/**
 * Сортировка расходов.
 */
private fun sortExpenses(
    expenses: List<Expense>,
    sortType: ExpenseSortType
): List<Expense> {
    return when (sortType) {
        ExpenseSortType.AMOUNT_DESC -> expenses.sortedByDescending { expense ->
            expense.amount
        }

        ExpenseSortType.AMOUNT_ASC -> expenses.sortedBy { expense ->
            expense.amount
        }

        ExpenseSortType.DATE_DESC -> expenses.sortedByDescending { expense ->
            parseExpenseDate(expense.date)
        }

        ExpenseSortType.DATE_ASC -> expenses.sortedBy { expense ->
            parseExpenseDate(expense.date)
        }

        ExpenseSortType.CATEGORY -> expenses.sortedBy { expense ->
            expense.category.displayName
        }
    }
}

/**
 * Парсинг даты расхода.
 */
private fun parseExpenseDate(
    date: String
): Long {
    val formats = listOf(
        "dd.MM.yyyy",
        "dd.MM.yyyy HH:mm",
        "yyyy-MM-dd"
    )

    formats.forEach { pattern ->
        try {
            val parsed = SimpleDateFormat(
                pattern,
                Locale.getDefault()
            ).parse(date)

            if (parsed != null) {
                return parsed.time
            }
        } catch (_: Exception) {
        }
    }

    return 0L
}

/**
 * Форматирует сумму.
 */
private fun formatAmount(
    amount: Double
): String {
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        amount.toString()
    }
}

/**
 * Форматирует сумму для поля ввода.
 */
private fun formatAmountInput(
    amount: Double
): String {
    return formatAmount(amount)
}

@Preview(
    showBackground = true,
    name = "BudgetTab - список расходов"
)
@Composable
private fun BudgetTabPreview() {
    TravelAppTheme {
        BudgetTab(
            tripId = "trip-1",
            uiState = BudgetUiState(
                totalAmount = 18200.0,
                budgetLimit = 30000.0,
                budgetLimitInput = "30000",
                sortType = ExpenseSortType.DATE_DESC,
                title = "Отель на 3 ночи",
                selectedCategory = ExpenseCategory.HOTEL,
                amount = "12000",
                expenseDate = "12.05.2026",
                expenses = listOf(
                    Expense(
                        id = "1",
                        tripId = "trip-1",
                        title = "Отель на 3 ночи рядом с центром города",
                        category = ExpenseCategory.HOTEL,
                        amount = 12000.0,
                        userId = "user-1",
                        date = "12.05.2026",
                        ownerType = ExpenseOwnerType.COMMON
                    ),
                    Expense(
                        id = "2",
                        tripId = "trip-1",
                        title = "Такси до вокзала утром",
                        category = ExpenseCategory.TAXI,
                        amount = 1200.0,
                        userId = "user-1",
                        date = "13.05.2026",
                        ownerType = ExpenseOwnerType.PERSONAL,
                        ownerUserId = "user-2",
                        ownerEmail = "friend@mail.ru"
                    ),
                    Expense(
                        id = "3",
                        tripId = "trip-1",
                        title = "Ужин в кафе после экскурсии",
                        category = ExpenseCategory.CAFE,
                        amount = 5000.0,
                        userId = "user-1",
                        date = "14.05.2026",
                        ownerType = ExpenseOwnerType.COMMON
                    )
                )
            ),
            participants = listOf(
                TripParticipant(
                    id = "user-1",
                    tripId = "trip-1",
                    email = "denis@mail.ru",
                    role = ParticipantRole.ORGANIZER,
                    status = ParticipantStatus.ACCEPTED
                ),
                TripParticipant(
                    id = "user-2",
                    tripId = "trip-1",
                    email = "friend@mail.ru",
                    role = ParticipantRole.EDITOR,
                    status = ParticipantStatus.ACCEPTED
                )
            ),
            canEdit = true,
            onTitleChange = {},
            onCategoryChange = {},
            onAmountChange = {},
            onExpenseDateChange = {},
            onOwnerTypeChange = {},
            onOwnerUserChange = { _, _ -> },
            onSortTypeChange = {},
            onBudgetLimitInputChange = {},
            onSaveBudgetLimitClick = {},
            onAddExpenseClick = {},
            onExpenseAddedHandled = {},
            onUpdateExpenseClick = { _, _, _, _, _, _, _, _ -> },
            onDeleteExpenseClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "BudgetTab - редактирование расхода"
)
@Composable
private fun EditExpenseSheetPreview() {
    TravelAppTheme {
        EditExpenseSheetContent(
            expense = Expense(
                id = "1",
                tripId = "trip-1",
                title = "Отель на 3 ночи рядом с центром города",
                category = ExpenseCategory.HOTEL,
                amount = 12000.0,
                userId = "user-1",
                date = "12.05.2026",
                ownerType = ExpenseOwnerType.COMMON
            ),
            participants = listOf(
                TripParticipant(
                    id = "user-1",
                    tripId = "trip-1",
                    email = "denis@mail.ru",
                    role = ParticipantRole.ORGANIZER,
                    status = ParticipantStatus.ACCEPTED
                )
            ),
            isLoading = false,
            onSaveClick = { _, _, _, _, _, _, _ -> },
            onDeleteClick = {}
        )
    }
}