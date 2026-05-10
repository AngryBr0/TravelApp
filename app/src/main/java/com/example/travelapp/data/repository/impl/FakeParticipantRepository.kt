package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.TripParticipant
import com.example.travelapp.data.repository.ParticipantRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * FakeParticipantRepository — временная реализация ParticipantRepository.
 *
 * Этот класс хранит участников поездки в памяти приложения.
 * Он нужен, чтобы реализовать вкладку "Участники" без Firebase.
 *
 * Позже мы создадим FirebaseParticipantRepository,
 * а ViewModel и экран менять не придется,
 * потому что они зависят от интерфейса ParticipantRepository.
 */
class FakeParticipantRepository : ParticipantRepository {

    /**
     * Хранилище участников по поездкам.
     *
     * Ключ Map — id поездки.
     * Значение — поток со списком участников этой поездки.
     */
    private val participantsByTrip =
        mutableMapOf<String, MutableStateFlow<AppResult<List<TripParticipant>>>>()

    /**
     * Возвращает Flow участников для конкретной поездки.
     *
     * Если участников для поездки еще нет, создается пустой список.
     */
    private fun getParticipantsFlow(
        tripId: String
    ): MutableStateFlow<AppResult<List<TripParticipant>>> {
        return participantsByTrip.getOrPut(tripId) {
            MutableStateFlow(AppResult.Success(emptyList()))
        }
    }

    /**
     * Добавляет участника в поездку.
     *
     * В настоящей Firebase-версии здесь будет запись
     * в подколлекцию participants внутри конкретной поездки.
     */
    override suspend fun inviteParticipant(
        tripId: String,
        participant: TripParticipant
    ): AppResult<Unit> {
        delay(300)

        val flow = getParticipantsFlow(tripId)

        val currentParticipants = when (val result = flow.value) {
            is AppResult.Success -> result.data
            else -> emptyList()
        }

        val newParticipant = participant.copy(
            id = System.currentTimeMillis().toString(),
            tripId = tripId
        )

        flow.value = AppResult.Success(currentParticipants + newParticipant)

        return AppResult.Success(Unit)
    }

    /**
     * Возвращает поток участников конкретной поездки.
     *
     * Flow нужен, чтобы экран автоматически обновлялся
     * после добавления нового участника.
     */
    override fun observeParticipants(
        tripId: String
    ): Flow<AppResult<List<TripParticipant>>> {
        return getParticipantsFlow(tripId)
    }
}