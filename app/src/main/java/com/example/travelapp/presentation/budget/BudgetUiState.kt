package com.example.travelapp.presentation.budget

import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory

/**
 * BudgetUiState — состояние вкладки бюджета.
 */
data class BudgetUiState(
    val isLoading: Boolean = false,

    val expenses: List<Expense> = emptyList(),

    val totalAmount: Double = 0.0,

    val title: String = "",
    val selectedCategory: ExpenseCategory = ExpenseCategory.FOOD,
    val amount: String = "",

    /**
     * Одноразовый флаг успешного добавления расхода.
     *
     * Нужен UI, чтобы понять:
     * расход добавлен успешно → можно закрыть bottom sheet.
     */
    val isExpenseAdded: Boolean = false,

    val errorMessage: String? = null
)