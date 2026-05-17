package com.example.travelapp.data.model

/**
 * ExpenseOwnerType — тип принадлежности расхода.
 *
 * COMMON — общий расход всей поездки.
 * PERSONAL — личный расход конкретного участника.
 */
enum class ExpenseOwnerType(
    val displayName: String
) {
    COMMON("Общий"),
    PERSONAL("Личный")
}