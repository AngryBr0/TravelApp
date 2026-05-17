package com.example.travelapp.data.model

/**
 * ParticipantStatus — статус участника поездки.
 *
 * INVITED — приглашение отправлено, но пользователь ещё не принял.
 * ACCEPTED — пользователь принял приглашение.
 * DECLINED — пользователь отклонил приглашение.
 */
enum class ParticipantStatus {
    INVITED,
    ACCEPTED,
    DECLINED
}