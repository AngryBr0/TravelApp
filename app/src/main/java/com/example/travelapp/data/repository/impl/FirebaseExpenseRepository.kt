package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.repository.ExpenseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseExpenseRepository — реальная реализация ExpenseRepository через Firestore.
 *
 * Этот класс отвечает за расходы конкретной поездки.
 *
 * Структура хранения:
 *
 * trips/
 *   tripId/
 *     expenses/
 *       expenseId/
 *         id
 *         tripId
 *         title
 *         category
 *         amount
 *         userId
 *         date
 *
 * ViewModel работает только через интерфейс ExpenseRepository,
 * поэтому экрану не важно, где именно хранятся данные.
 */
class FirebaseExpenseRepository(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    /**
     * Возвращает ссылку на подколлекцию expenses конкретной поездки.
     */
    private fun expensesCollection(tripId: String) =
        firestore
            .collection("trips")
            .document(tripId)
            .collection("expenses")

    /**
     * Добавляет новый расход в Firestore.
     */
    override suspend fun addExpense(
        tripId: String,
        expense: Expense
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (expense.title.isBlank()) {
                return AppResult.Error("Введите название расхода")
            }

            if (expense.amount <= 0.0) {
                return AppResult.Error("Введите корректную сумму")
            }

            val document = expensesCollection(tripId).document()

            val expenseWithId = expense.copy(
                id = document.id,
                tripId = tripId
            )

            /**
             * enum ExpenseCategory сохраняем как строку через .name.
             * Так проще читать и восстанавливать категорию обратно.
             */
            val expenseMap = mapOf(
                "id" to expenseWithId.id,
                "tripId" to expenseWithId.tripId,
                "title" to expenseWithId.title,
                "category" to expenseWithId.category.name,
                "amount" to expenseWithId.amount,
                "userId" to expenseWithId.userId,
                "date" to expenseWithId.date
            )

            document.set(expenseMap).await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка добавления расхода"
            )
        }
    }

    /**
     * Подписывается на список расходов конкретной поездки.
     *
     * addSnapshotListener обновляет экран автоматически,
     * когда в Firestore добавляется или удаляется расход.
     */
    override fun observeExpenses(
        tripId: String
    ): Flow<AppResult<List<Expense>>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = expensesCollection(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки расходов"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(AppResult.Success(emptyList()))
                    return@addSnapshotListener
                }

                val expenses = snapshot.documents
                    .mapNotNull { document ->
                        document.toExpenseOrNull()
                    }

                trySend(AppResult.Success(expenses))
            }

        /**
         * Когда Flow больше не используется,
         * удаляем Firebase listener.
         */
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Удаляет расход из Firestore.
     */
    override suspend fun deleteExpense(
        tripId: String,
        expenseId: String
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank() || expenseId.isBlank()) {
                return AppResult.Error("Не указан id расхода")
            }

            expensesCollection(tripId)
                .document(expenseId)
                .delete()
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка удаления расхода"
            )
        }
    }

    /**
     * Преобразует документ Firestore в объект Expense.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toExpenseOrNull(): Expense? {
        val id = getString("id") ?: this.id
        val title = getString("title") ?: return null

        val categoryText = getString("category") ?: ExpenseCategory.OTHER.name

        val category = runCatching {
            ExpenseCategory.valueOf(categoryText)
        }.getOrDefault(ExpenseCategory.OTHER)

        return Expense(
            id = id,
            tripId = getString("tripId").orEmpty(),
            title = title,
            category = category,
            amount = getDouble("amount") ?: 0.0,
            userId = getString("userId").orEmpty(),
            date = getString("date").orEmpty()
        )
    }
}