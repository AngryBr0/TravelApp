package com.example.travelapp.presentation.budget

import com.example.travelapp.data.model.NotificationItem
import com.example.travelapp.data.repository.NotificationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.ExpenseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BudgetViewModel отвечает за вкладку бюджета.
 *
 * Она:
 * - хранит состояние вкладки;
 * - получает расходы из репозитория;
 * - добавляет новые расходы;
 * - удаляет расходы;
 * - считает общую сумму.
 */
class BudgetViewModel(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    /**
     * Job нужен, чтобы не создавать несколько подписок
     * на расходы одной и той же поездки.
     */
    private var observeJob: Job? = null

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    /**
     * Загружает расходы выбранной поездки.
     */
    fun loadExpenses(tripId: String) {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            expenseRepository.observeExpenses(tripId).collect { result ->
                when (result) {
                    AppResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is AppResult.Success -> {
                        val expenses = result.data
                        val total = expenses.sumOf { expense ->
                            expense.amount
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            expenses = expenses,
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
    }

    /**
     * Добавляет новый расход.
     */
    fun addExpense(tripId: String) {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Введите название расхода"
            )
            return
        }

        val amount = state.amount
            .replace(",", ".")
            .toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _uiState.value = state.copy(
                errorMessage = "Введите корректную сумму"
            )
            return
        }

        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.value = state.copy(
                errorMessage = "Пользователь не авторизован"
            )
            return
        }

        val expense = Expense(
            tripId = tripId,
            title = state.title,
            category = parseCategory(state.category),
            amount = amount,
            userId = userId,
            date = ""
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = expenseRepository.addExpense(tripId, expense)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = "",
                        category = "",
                        amount = "",
                        errorMessage = null
                    )
                    val userId = authRepository.getCurrentUserId()

                    if (userId != null) {
                        notificationRepository.addNotification(
                            NotificationItem(
                                userId = userId,
                                tripId = tripId,
                                text = "Добавлен расход: ${expense.title} — ${expense.amount} ₽",
                                createdAt = getCurrentDateTime(),
                                createdAtMillis = System.currentTimeMillis()
                            )
                        )
                    }
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
     * Преобразует текстовую категорию в enum ExpenseCategory.
     *
     * Пользователь может ввести категорию по-русски,
     * а внутри приложения мы храним ее как enum.
     */
    private fun parseCategory(category: String): ExpenseCategory {
        return when (category.trim().lowercase()) {
            "транспорт", "transport" -> ExpenseCategory.TRANSPORT
            "еда", "food" -> ExpenseCategory.FOOD
            "отель", "гостиница", "hotel" -> ExpenseCategory.HOTEL
            "развлечения", "entertainment" -> ExpenseCategory.ENTERTAINMENT
            else -> ExpenseCategory.OTHER
        }
    }
    /**
     * Возвращает текущую дату и время для уведомления.
     */
    private fun getCurrentDateTime(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
    }
}