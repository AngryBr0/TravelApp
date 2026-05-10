package com.example.travelapp.presentation.navigation

/**
 * Screen хранит маршруты экранов приложения.
 *
 * Это нужно, чтобы не писать строки маршрутов вручную по всему проекту.
 * Так меньше шанс ошибиться в названии экрана.
 */
sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Trips : Screen("trips")

    object CreateTrip : Screen("create_trip")

    object Profile : Screen("profile")

    object Notifications : Screen("notifications")

    /**
     * Экран конкретной поездки.
     *
     * trip/{tripId} означает, что в маршрут передается id поездки.
     */
    object TripDetails : Screen("trip/{tripId}") {

        /**
         * Создает реальный маршрут с конкретным id.
         *
         * Например:
         * trip/12345
         */
        fun createRoute(tripId: String): String {
            return "trip/$tripId"
        }
    }
}