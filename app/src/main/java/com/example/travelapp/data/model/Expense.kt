package com.example.travelapp.data.model

data class Expense(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: Double = 0.0,
    val userId: String = "",
    val date: String = ""
)