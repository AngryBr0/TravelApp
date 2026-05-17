package com.example.travelapp.presentation.budget

/**
 * ExpenseSortType — варианты сортировки расходов.
 */
enum class ExpenseSortType(
    val displayName: String
) {
    AMOUNT_DESC("Сумма: сначала большие"),
    AMOUNT_ASC("Сумма: сначала маленькие"),
    DATE_DESC("Дата: сначала новые"),
    DATE_ASC("Дата: сначала старые"),
    CATEGORY("По категории")
}