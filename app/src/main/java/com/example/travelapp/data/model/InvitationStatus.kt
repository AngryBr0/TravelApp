package com.example.travelapp.data.model

/**
 * InvitationStatus — статус приглашения в поездку.
 *
 * PENDING — приглашение создано, пользователь ещё не принял его.
 * ACCEPTED — пользователь принял приглашение.
 * DECLINED — пользователь отклонил приглашение.
 */
enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}