package com.example.travelapp.presentation.budget

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Expense
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.ExpenseRepository
import com.example.travelapp.data.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk(relaxed = true)
        expenseRepository = mockk(relaxed = true)
        notificationRepository = mockk(relaxed = true)

        viewModel = BudgetViewModel(
            authRepository = authRepository,
            expenseRepository = expenseRepository,
            notificationRepository = notificationRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addExpense_withEmptyAmount_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"

        viewModel.updateTitle("Обед")
        viewModel.updateAmount("")

        viewModel.addExpense("trip-1")

        assertFalse(viewModel.uiState.value.isExpenseAdded)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun addExpense_withInvalidAmount_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"

        viewModel.updateTitle("Обед")
        viewModel.updateAmount("abc")

        viewModel.addExpense("trip-1")

        assertFalse(viewModel.uiState.value.isExpenseAdded)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun addExpense_withValidData_addsExpenseSuccessfully() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"
        coEvery { expenseRepository.addExpense(any(), any<Expense>()) } returns AppResult.Success(Unit)

        viewModel.updateTitle("Обед")
        viewModel.updateAmount("1200")

        viewModel.addExpense("trip-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isExpenseAdded)

        coVerify(exactly = 1) {
            expenseRepository.addExpense("trip-1", any<Expense>())
        }
    }
}