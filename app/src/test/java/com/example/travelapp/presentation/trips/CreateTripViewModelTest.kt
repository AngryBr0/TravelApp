package com.example.travelapp.presentation.trips

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.TripRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTripViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var tripRepository: TripRepository
    private lateinit var viewModel: CreateTripViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk(relaxed = true)
        tripRepository = mockk(relaxed = true)

        viewModel = CreateTripViewModel(
            authRepository = authRepository,
            tripRepository = tripRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createTrip_withEmptyTitle_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"

        viewModel.updateTitle("")
        viewModel.updateDescription("Описание")
        viewModel.updateStartDate("12 мая 2026")
        viewModel.updateEndDate("14 мая 2026")

        viewModel.createTrip()

        assertEquals(
            "Введите название поездки",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun createTrip_withoutAuthorizedUser_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns null

        viewModel.updateTitle("Казань")
        viewModel.updateDescription("Описание")
        viewModel.updateStartDate("12 мая 2026")
        viewModel.updateEndDate("14 мая 2026")

        viewModel.createTrip()

        assertEquals(
            "Пользователь не авторизован",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun createTrip_withValidData_createsTripSuccessfully() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"
        coEvery { tripRepository.createTrip(any<Trip>()) } returns AppResult.Success(Unit)

        viewModel.updateTitle("Казань")
        viewModel.updateDescription("Поездка на выходные")
        viewModel.updateStartDate("12.05.2026")
        viewModel.updateEndDate("14.05.2026")

        viewModel.createTrip()
        testDispatcher.scheduler.advanceUntilIdle()


        assertTrue(viewModel.uiState.value.isCreated)
        assertFalse(viewModel.uiState.value.isLoading)

        coVerify(exactly = 1) {
            tripRepository.createTrip(any<Trip>())
        }
    }

    @Test
    fun createTrip_whenRepositoryReturnsError_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"
        coEvery { tripRepository.createTrip(any<Trip>()) } returns AppResult.Error("Ошибка сохранения поездки")

        viewModel.updateTitle("Казань")
        viewModel.updateDescription("Поездка на выходные")
        viewModel.updateStartDate("12.05.2026")
        viewModel.updateEndDate("14.05.2026")

        viewModel.createTrip()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCreated)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Ошибка сохранения поездки", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun createTrip_withEmptyStartDate_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"

        viewModel.updateTitle("Казань")
        viewModel.updateDescription("Поездка на выходные")
        viewModel.updateStartDate("")
        viewModel.updateEndDate("14.05.2026")

        viewModel.createTrip()

        assertFalse(viewModel.uiState.value.isCreated)
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }
}