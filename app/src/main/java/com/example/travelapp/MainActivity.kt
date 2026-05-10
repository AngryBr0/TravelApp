package com.example.travelapp

import com.yandex.mapkit.MapKitFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.presentation.navigation.AppNavigation


/**
 * MainActivity — главная Activity приложения.
 *
 * Activity — это точка входа в Android-приложение.
 * Когда пользователь запускает приложение, первым создается MainActivity.
 */
class MainActivity : ComponentActivity() {

    /**
     * onCreate вызывается при создании экрана приложения.
     *
     * override означает, что мы переопределяем метод,
     * который уже есть в базовом классе ComponentActivity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включает современный режим отображения приложения "от края до края"
        enableEdgeToEdge()

        /**
         * Инициализируем MapKit.
         *
         * По документации MapKitFactory.initialize(Context)
         * загружает необходимые нативные библиотеки MapKit.
         */
        MapKitFactory.initialize(this)
        /**
         * setContent запускает Jetpack Compose.
         *
         * Внутри setContent мы описываем, какой интерфейс
         * должен отображаться на экране.
         */
        setContent {
            /**
             * TravelAppTheme — тема приложения.
             *
             * Она задает цвета, шрифты и общий стиль интерфейса.
             */
            TravelAppTheme {
                /**
                 * AppNavigation отвечает за переходы между экранами:
                 * LoginScreen, RegisterScreen, TripsScreen,
                 * CreateTripScreen и TripScreen.
                 */
                AppNavigation()
            }
        }
    }
    /**
     * Передаем событие onStart в MapKit.
     */
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    /**
     * Передаем событие onStop в MapKit.
     */
    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
