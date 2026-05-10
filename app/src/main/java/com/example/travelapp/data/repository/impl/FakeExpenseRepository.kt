package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.repository.ExpenseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FakeExpenseRepository — временная реализация ExpenseRepository.
 *
 * Этот класс хранит расходы поездки в памяти приложения.
 * Он нужен для разработки интерфейса и ViewModel до подключения Firebase.
 *
 * Позже мы создадим FirebaseExpenseRepository,
 * но экран и ViewModel менять не придется, потому что они работают
 * через интерфейс ExpenseRepository.
 */
class FakeExpenseRepository : ExpenseRepository {

    /**
     * Хранилище расходов по поездкам.
     *
     * Ключ Map — id поездки.
     * Значение — поток со списком расходов этой поездки.
     */
    private val expensesByTrip =
        mutableMapOf<String, MutableStateFlow<AppResult<List<Expense>>>>()

    /**
     * Получает Flow расходов для конкретной поездки.
     *
     * Если расходов для поездки еще нет, создается пустой список.
     */
    private fun getExpensesFlow(
        tripId: String
    ): MutableStateFlow<AppResult<List<Expense>>> {
        return expensesByTrip.getOrPut(tripId) {
            MutableStateFlow(AppResult.Success(emptyList()))
        }
    }

    /**
     * Добавляет новый расход в поездку.
     */
    override suspend fun addExpense(
        tripId: String,
        expense: Expense
    ): AppResult<Unit> {
        delay(300) // имитируем задержку сети

        val flow = getExpensesFlow(tripId)

        val currentExpenses = when (val result = flow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        val newExpense = expense.copy(
            id = System.currentTimeMillis().toString(),
            tripId = tripId
        )

        flow.value = AppResult.Success(currentExpenses + newExpense)

        return AppResult.Success(Unit)
    }

    /**
     * Возвращает поток расходов конкретной поездки.
     *
     * Flow нужен, чтобы экран автоматически обновлялся
     * после добавления или удаления расхода.
     */
    override fun observeExpenses(
        tripId: String
    ): Flow<AppResult<List<Expense>>> {
        return getExpensesFlow(tripId)
    }

    /**
     * Удаляет расход из поездки.
     */
    override suspend fun deleteExpense(
        tripId: String,
        expenseId: String
    ): AppResult<Unit> {
        val flow = getExpensesFlow(tripId)

        val currentExpenses = when (val result = flow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        flow.value = AppResult.Success(
            currentExpenses.filter { expense ->
                expense.id != expenseId
            }
        )

        return AppResult.Success(Unit)
    }
}