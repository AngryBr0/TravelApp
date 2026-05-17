package com.example.travelapp.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.model.ExpenseOwnerType
import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.ExpenseRepository
import com.example.travelapp.data.repository.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BudgetViewModel отвечает за вкладку бюджета.
 *
 * Она:
 * - хранит состояние вкладки;
 * - получает расходы из репозитория;
 * - добавляет новые расходы;
 * - редактирует расходы;
 * - удаляет расходы;
 * - считает общую сумму;
 * - хранит лимит бюджета поездки;
 * - сортирует расходы.
 */
class BudgetViewModel(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private var observeExpensesJob: Job? = null
    private var observeBudgetLimitJob: Job? = null

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            errorMessage = null,
            isExpenseAdded = false
        )
    }

    fun updateCategory(category: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            errorMessage = null,
            isExpenseAdded = false
        )
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(
            amount = amount,
            errorMessage = null,
            isExpenseAdded = false
        )
    }

    fun updateExpenseDate(date: String) {
        _uiState.value = _uiState.value.copy(
            expenseDate = date,
            errorMessage = null,
            isExpenseAdded = false
        )
    }

    fun updateOwnerType(ownerType: ExpenseOwnerType) {
        _uiState.value = _uiState.value.copy(
            selectedOwnerType = ownerType,
            selectedOwnerUserId = "",
            selectedOwnerEmail = "",
            errorMessage = null,
            isExpenseAdded = false
        )
    }

    fun updateOwnerUser(
        userId: String,
        email: String
    ) {
        _uiState.value = _uiState.value.copy(
            selectedOwnerType = ExpenseOwnerType.PERSONAL,
            selectedOwnerUserId = userId,
            selectedOwnerEmail = email,
            errorMessage = null,
            isExpenseAdded = false
        )
    }

    fun updateSortType(sortType: ExpenseSortType) {
        val state = _uiState.value

        _uiState.value = state.copy(
            sortType = sortType,
            expenses = sortExpenses(
                expenses = state.expenses,
                sortType = sortType
            )
        )
    }

    fun updateBudgetLimitInput(value: String) {
        _uiState.value = _uiState.value.copy(
            budgetLimitInput = value,
            errorMessage = null
        )
    }

    /**
     * Загружает расходы и лимит бюджета выбранной поездки.
     */
    fun loadExpenses(tripId: String) {
        observeExpensesJob?.cancel()
        observeBudgetLimitJob?.cancel()

        observeExpensesJob = viewModelScope.launch {
            expenseRepository.observeExpenses(tripId).collect { result ->
                when (result) {
                    AppResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is AppResult.Success -> {
                        val sortedExpenses = sortExpenses(
                            expenses = result.data,
                            sortType = _uiState.value.sortType
                        )

                        val total = sortedExpenses.sumOf { expense ->
                            expense.amount
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            expenses = sortedExpenses,
                            totalAmount = total,
                            errorMessage = null
                        )
                    }

                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }

        observeBudgetLimitJob = viewModelScope.launch {
            expenseRepository.observeBudgetLimit(tripId).collect { result ->
                when (result) {
                    AppResult.Loading -> Unit

                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            budgetLimit = result.data,
                            budgetLimitInput = formatAmountInput(result.data)
                        )
                    }

                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Сохраняет общий бюджет поездки.
     */
    fun saveBudgetLimit(tripId: String) {
        val state = _uiState.value

        val budget = state.budgetLimitInput
            .replace(",", ".")
            .toDoubleOrNull()

        if (budget == null || budget < 0.0) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректный бюджет"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = expenseRepository.updateBudgetLimit(
                    tripId = tripId,
                    budgetLimit = budget
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        budgetLimit = budget,
                        budgetLimitInput = formatAmountInput(budget),
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Добавляет новый расход.
     */
    fun addExpense(tripId: String) {
        val state = _uiState.value

        val amount = state.amount
            .replace(",", ".")
            .toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _uiState.value = state.copy(
                isLoading = false,
                isExpenseAdded = false,
                errorMessage = "Введите корректную сумму"
            )
            return
        }

        if (
            state.selectedOwnerType == ExpenseOwnerType.PERSONAL &&
            state.selectedOwnerUserId.isBlank()
        ) {
            _uiState.value = state.copy(
                isLoading = false,
                isExpenseAdded = false,
                errorMessage = "Выберите участника для личного расхода"
            )
            return
        }

        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = state.copy(
                isLoading = false,
                isExpenseAdded = false,
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        val expenseDate = state.expenseDate.ifBlank {
            getCurrentDate()
        }

        val expense = Expense(
            tripId = tripId,
            title = state.title.trim(),
            category = state.selectedCategory,
            amount = amount,
            userId = userId,
            date = expenseDate,
            ownerType = state.selectedOwnerType,
            ownerUserId = if (state.selectedOwnerType == ExpenseOwnerType.PERSONAL) {
                state.selectedOwnerUserId
            } else {
                ""
            },
            ownerEmail = if (state.selectedOwnerType == ExpenseOwnerType.PERSONAL) {
                state.selectedOwnerEmail
            } else {
                ""
            }
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null,
                isExpenseAdded = false
            )

            when (
                val result = expenseRepository.addExpense(
                    tripId = tripId,
                    expense = expense
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = "",
                        amount = "",
                        expenseDate = "",
                        selectedCategory = ExpenseCategory.FOOD,
                        selectedOwnerType = ExpenseOwnerType.COMMON,
                        selectedOwnerUserId = "",
                        selectedOwnerEmail = "",
                        errorMessage = null,
                        isExpenseAdded = true
                    )

                    notificationRepository.addNotification(
                        NotificationItem(
                            userId = userId,
                            tripId = tripId,
                            text = "Добавлен расход: ${
                                expense.title.ifBlank {
                                    expense.category.displayName
                                }
                            } — ${formatAmountInput(expense.amount)} ₽",
                            createdAt = getCurrentDateTime()
                        )
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isExpenseAdded = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Обновляет существующий расход.
     */
    fun updateExpense(
        tripId: String,
        expenseId: String,
        description: String,
        category: ExpenseCategory,
        amountText: String,
        date: String,
        ownerType: ExpenseOwnerType,
        ownerUserId: String,
        ownerEmail: String
    ) {
        val state = _uiState.value

        val amount = amountText
            .replace(",", ".")
            .toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректную сумму"
            )
            return
        }

        if (ownerType == ExpenseOwnerType.PERSONAL && ownerUserId.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Выберите участника для личного расхода"
            )
            return
        }

        val currentExpense = state.expenses.firstOrNull { expense ->
            expense.id == expenseId
        }

        if (currentExpense == null) {
            _uiState.value = state.copy(
                errorMessage = "Расход не найден"
            )
            return
        }

        val updatedExpense = currentExpense.copy(
            title = description.trim(),
            category = category,
            amount = amount,
            date = date.ifBlank {
                currentExpense.date
            },
            ownerType = ownerType,
            ownerUserId = if (ownerType == ExpenseOwnerType.PERSONAL) {
                ownerUserId
            } else {
                ""
            },
            ownerEmail = if (ownerType == ExpenseOwnerType.PERSONAL) {
                ownerEmail
            } else {
                ""
            }
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = expenseRepository.updateExpense(
                    tripId = tripId,
                    expense = updatedExpense
                )
            ) {
                is AppResult.Success -> {
                    val updatedExpenses = _uiState.value.expenses.map { expense ->
                        if (expense.id == expenseId) {
                            updatedExpense
                        } else {
                            expense
                        }
                    }

                    val sortedExpenses = sortExpenses(
                        expenses = updatedExpenses,
                        sortType = _uiState.value.sortType
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        expenses = sortedExpenses,
                        totalAmount = sortedExpenses.sumOf { expense ->
                            expense.amount
                        },
                        errorMessage = null
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AppResult.Loading -> Unit
            }
        }
    }

    /**
     * Удаляет расход.
     */
    fun deleteExpense(
        tripId: String,
        expenseId: String
    ) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(
                tripId = tripId,
                expenseId = expenseId
            )
        }
    }

    /**
     * Сбрасывает одноразовый флаг успешного добавления расхода.
     */
    fun consumeExpenseAddedEvent() {
        _uiState.value = _uiState.value.copy(
            isExpenseAdded = false
        )
    }

    private fun sortExpenses(
        expenses: List<Expense>,
        sortType: ExpenseSortType
    ): List<Expense> {
        return when (sortType) {
            ExpenseSortType.AMOUNT_DESC -> {
                expenses.sortedByDescending { expense ->
                    expense.amount
                }
            }

            ExpenseSortType.AMOUNT_ASC -> {
                expenses.sortedBy { expense ->
                    expense.amount
                }
            }

            ExpenseSortType.DATE_DESC -> {
                expenses.sortedByDescending { expense ->
                    parseExpenseDate(expense.date)
                }
            }

            ExpenseSortType.DATE_ASC -> {
                expenses.sortedBy { expense ->
                    parseExpenseDate(expense.date)
                }
            }

            ExpenseSortType.CATEGORY -> {
                expenses.sortedBy { expense ->
                    expense.category.displayName
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(Date())
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
    }

    private fun parseExpenseDate(date: String): Long {
        val formats = listOf(
            "dd.MM.yyyy",
            "dd.MM.yyyy HH:mm",
            "yyyy-MM-dd"
        )

        formats.forEach { pattern ->
            try {
                val parsedDate = SimpleDateFormat(
                    pattern,
                    Locale.getDefault()
                ).parse(date)

                if (parsedDate != null) {
                    return parsedDate.time
                }
            } catch (_: Exception) {
            }
        }

        return 0L
    }

    private fun formatAmountInput(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            amount.toString()
        }
    }
}