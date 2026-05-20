package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(
        tripId: String
    ): Flow<AppResult<List<Expense>>>
    suspend fun addExpense(
        tripId: String,
        expense: Expense
    ): AppResult<Unit>
    suspend fun updateExpense(
        tripId: String,
        expense: Expense
    ): AppResult<Unit>
    suspend fun deleteExpense(
        tripId: String,
        expenseId: String
    ): AppResult<Unit>
    fun observeBudgetLimit(
        tripId: String
    ): Flow<AppResult<Double>>
    suspend fun updateBudgetLimit(
        tripId: String,
        budgetLimit: Double
    ): AppResult<Unit>
}