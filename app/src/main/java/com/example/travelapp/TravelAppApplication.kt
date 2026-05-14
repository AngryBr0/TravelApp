package com.example.travelapp

import android.app.Application
import com.yandex.mapkit.MapKitFactory
/**
 * TravelAppApplication — класс приложения.
 *
 * Он создается раньше MainActivity.
 * Здесь удобно один раз установить API-ключ MapKit.
 */
class TravelAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * Устанавливаем API-ключ Яндекс MapKit.
         *
         * Ключ берется из BuildConfig, а BuildConfig получает его
         * из local.properties. Так мы не храним ключ в GitHub.
         */
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}