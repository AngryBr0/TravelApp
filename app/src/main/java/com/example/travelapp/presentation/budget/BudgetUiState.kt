package com.example.travelapp.presentation.budget

import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.model.ExpenseOwnerType

/**
 * BudgetUiState — состояние вкладки бюджета.
 */
data class BudgetUiState(
    val isLoading: Boolean = false,

    val expenses: List<Expense> = emptyList(),
    val totalAmount: Double = 0.0,

    val budgetLimit: Double = 0.0,
    val budgetLimitInput: String = "",

    val title: String = "",
    val selectedCategory: ExpenseCategory = ExpenseCategory.FOOD,
    val amount: String = "",
    val expenseDate: String = "",

    val selectedOwnerType: ExpenseOwnerType = ExpenseOwnerType.COMMON,
    val selectedOwnerUserId: String = "",
    val selectedOwnerEmail: String = "",

    val sortType: ExpenseSortType = ExpenseSortType.DATE_DESC,

    /**
     * Одноразовый флаг успешного добавления расхода.
     *
     * Нужен UI, чтобы понять:
     * расход добавлен успешно → можно закрыть bottom sheet.
     */
    val isExpenseAdded: Boolean = false,

    val errorMessage: String? = null
)