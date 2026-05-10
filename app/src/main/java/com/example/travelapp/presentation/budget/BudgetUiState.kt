package com.example.travelapp.presentation.budget

import com.example.travelapp.data.model.Expense

/**
 * BudgetUiState — состояние вкладки бюджета.
 *
 * Здесь хранятся:
 * - список расходов;
 * - общая сумма;
 * - значения полей формы;
 * - состояние загрузки;
 * - текст ошибки.
 */
data class BudgetUiState(
    val isLoading: Boolean = false,

    val expenses: List<Expense> = emptyList(),

    val totalAmount: Double = 0.0,

    val title: String = "",
    val category: String = "",
    val amount: String = "",

    val errorMessage: String? = null
)