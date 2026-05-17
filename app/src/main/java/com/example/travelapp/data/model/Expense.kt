package com.example.travelapp.data.model

/**
 * Expense — расход поездки.
 *
 * title используется как описание расхода.
 * ownerType показывает, общий это расход или личный.
 * ownerUserId / ownerEmail используются только для личных расходов.
 */
data class Expense(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: Double = 0.0,
    val userId: String = "",
    val date: String = "",

    val ownerType: ExpenseOwnerType = ExpenseOwnerType.COMMON,
    val ownerUserId: String = "",
    val ownerEmail: String = ""
)