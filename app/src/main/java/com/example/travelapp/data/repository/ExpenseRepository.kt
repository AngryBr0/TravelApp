package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    suspend fun addExpense(
        tripId: String,
        expense: Expense
    ): AppResult<Unit>

    fun observeExpenses(tripId: String): Flow<AppResult<List<Expense>>>

    suspend fun deleteExpense(
        tripId: String,
        expenseId: String
    ): AppResult<Unit>
}