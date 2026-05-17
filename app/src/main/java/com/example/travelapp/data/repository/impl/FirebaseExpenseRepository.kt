package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.model.ExpenseCategory
import com.example.travelapp.data.model.ExpenseOwnerType
import com.example.travelapp.data.repository.ExpenseRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseExpenseRepository — реализация ExpenseRepository через Firestore.
 *
 * Расходы хранятся здесь:
 *
 * trips/
 *   tripId/
 *     expenses/
 *       expenseId/
 *
 * Бюджет поездки хранится прямо в документе поездки:
 *
 * trips/
 *   tripId/
 *     budgetLimit
 */
class FirebaseExpenseRepository(
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    /**
     * Ссылка на подколлекцию расходов конкретной поездки.
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

            if (expense.amount <= 0.0) {
                return AppResult.Error("Введите корректную сумму")
            }

            val document = expensesCollection(tripId).document()

            val expenseWithId = expense.copy(
                id = document.id,
                tripId = tripId
            )

            val expenseMap = mapOf(
                "id" to expenseWithId.id,
                "tripId" to expenseWithId.tripId,
                "title" to expenseWithId.title.trim(),
                "category" to expenseWithId.category.name,
                "amount" to expenseWithId.amount,
                "userId" to expenseWithId.userId,
                "date" to expenseWithId.date,

                /**
                 * Новые поля:
                 * COMMON — общий расход поездки.
                 * PERSONAL — личный расход конкретного участника.
                 */
                "ownerType" to expenseWithId.ownerType.name,
                "ownerUserId" to expenseWithId.ownerUserId,
                "ownerEmail" to expenseWithId.ownerEmail
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

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Обновляет существующий расход.
     */
    override suspend fun updateExpense(
        tripId: String,
        expense: Expense
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (expense.id.isBlank()) {
                return AppResult.Error("Расход не найден")
            }

            if (expense.amount <= 0.0) {
                return AppResult.Error("Введите корректную сумму")
            }

            expensesCollection(tripId)
                .document(expense.id)
                .update(
                    mapOf(
                        "title" to expense.title.trim(),
                        "category" to expense.category.name,
                        "amount" to expense.amount,
                        "date" to expense.date,
                        "ownerType" to expense.ownerType.name,
                        "ownerUserId" to expense.ownerUserId,
                        "ownerEmail" to expense.ownerEmail
                    )
                )
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка обновления расхода"
            )
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
     * Подписывается на бюджет поездки.
     *
     * budgetLimit хранится в документе trips/{tripId}.
     */
    override fun observeBudgetLimit(
        tripId: String
    ): Flow<AppResult<Double>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = firestore
            .collection("trips")
            .document(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки бюджета поездки"
                        )
                    )
                    return@addSnapshotListener
                }

                val budgetLimit = snapshot
                    ?.getDouble("budgetLimit")
                    ?: 0.0

                trySend(AppResult.Success(budgetLimit))
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Сохраняет общий бюджет поездки.
     */
    override suspend fun updateBudgetLimit(
        tripId: String,
        budgetLimit: Double
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (budgetLimit < 0.0) {
                return AppResult.Error("Бюджет не может быть отрицательным")
            }

            firestore
                .collection("trips")
                .document(tripId)
                .update("budgetLimit", budgetLimit)
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка сохранения бюджета поездки"
            )
        }
    }

    /**
     * Преобразует документ Firestore в объект Expense.
     *
     * Важно:
     * title теперь может быть пустым, потому что описание расхода необязательное.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toExpenseOrNull(): Expense? {
        val id = getString("id") ?: this.id

        val categoryText = getString("category")
            ?: ExpenseCategory.OTHER.name

        val category = runCatching {
            ExpenseCategory.valueOf(categoryText)
        }.getOrDefault(ExpenseCategory.OTHER)

        val ownerTypeText = getString("ownerType")
            ?: ExpenseOwnerType.COMMON.name

        val ownerType = runCatching {
            ExpenseOwnerType.valueOf(ownerTypeText)
        }.getOrDefault(ExpenseOwnerType.COMMON)

        return Expense(
            id = id,
            tripId = getString("tripId").orEmpty(),
            title = getString("title").orEmpty(),
            category = category,
            amount = getDouble("amount") ?: 0.0,
            userId = getString("userId").orEmpty(),
            date = getString("date").orEmpty(),
            ownerType = ownerType,
            ownerUserId = getString("ownerUserId").orEmpty(),
            ownerEmail = getString("ownerEmail").orEmpty()
        )
    }
}