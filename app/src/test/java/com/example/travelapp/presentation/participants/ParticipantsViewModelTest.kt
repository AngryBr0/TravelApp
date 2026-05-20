package com.example.travelapp.presentation.participants

import com.example.travelapp.data.repository.AuthRepository
import com.example.travelapp.data.repository.InvitationRepository
import com.example.travelapp.data.repository.NotificationRepository
import com.example.travelapp.data.repository.ParticipantRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParticipantsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var participantRepository: ParticipantRepository
    private lateinit var invitationRepository: InvitationRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: ParticipantsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        participantRepository = mockk(relaxed = true)
        invitationRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        notificationRepository = mockk(relaxed = true)

        viewModel = ParticipantsViewModel(
            participantRepository = participantRepository,
            invitationRepository = invitationRepository,
            authRepository = authRepository,
            notificationRepository = notificationRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun inviteParticipant_withEmptyEmail_showsError() = runTest {
        every { authRepository.getCurrentUserId() } returns "user-1"

        viewModel.updateEmail("")
        viewModel.inviteParticipant(
            tripId = "trip-1",
            tripTitle = "Казань"
        )

        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}