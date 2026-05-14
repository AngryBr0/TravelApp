package com.example.travelapp.presentation.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.travelapp.data.model.RoutePoint

/**
 * YandexMapsExporter — открывает построенный маршрут
 * во внешнем приложении Яндекс Карты.
 *
 * Работает так:
 * 1. Сначала пробует открыть yandexmaps://
 * 2. Если не получилось — открывает веб-ссылку Яндекс Карт
 * 3. Если и это не получилось — пробует открыть Google Play
 */
class YandexMapsExporter {

    fun openRouteInYandexMaps(
        context: Context,
        routePoints: List<RoutePoint>
    ): Boolean {
        val sortedPoints = routePoints
            .filter { point ->
                point.latitude != 0.0 || point.longitude != 0.0
            }
            .sortedBy { point ->
                point.order
            }

        if (sortedPoints.size < 2) {
            return false
        }

        val appUri = buildYandexMapsAppUri(sortedPoints)
        val webUri = buildYandexMapsWebUri(sortedPoints)

        /**
         * 1. Пробуем открыть именно приложение Яндекс Карты.
         */
        val appIntent = Intent(
            Intent.ACTION_VIEW,
            appUri
        ).apply {
            setPackage(YANDEX_MAPS_PACKAGE)
        }

        if (tryStartActivity(context, appIntent)) {
            return true
        }

        /**
         * 2. Если приложение не открылось, пробуем открыть веб-ссылку.
         *
         * На телефоне она часто сама перекидывает в установленное приложение
         * или открывает маршрут в браузере.
         */
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            webUri
        )

        if (tryStartActivity(context, webIntent)) {
            return true
        }

        /**
         * 3. Если даже веб-ссылка не открылась, пробуем открыть страницу в магазине.
         */
        openYandexMapsInStore(context)

        return false
    }

    /**
     * URI для приложения Яндекс Карты.
     *
     * Формат:
     * yandexmaps://maps.yandex.ru/?rtext=lat1,lon1~lat2,lon2&rtt=auto
     */
    private fun buildYandexMapsAppUri(
        sortedPoints: List<RoutePoint>
    ): Uri {
        val routeText = buildRouteText(sortedPoints)

        return Uri
            .parse("yandexmaps://maps.yandex.ru/")
            .buildUpon()
            .appendQueryParameter("rtext", routeText)
            .appendQueryParameter("rtt", "auto")
            .build()
    }

    /**
     * Веб-ссылка Яндекс Карт.
     *
     * Используется как fallback, если приложение не открылось напрямую.
     */
    private fun buildYandexMapsWebUri(
        sortedPoints: List<RoutePoint>
    ): Uri {
        val routeText = buildRouteText(sortedPoints)

        return Uri
            .parse("https://yandex.ru/maps/")
            .buildUpon()
            .appendQueryParameter("rtext", routeText)
            .appendQueryParameter("rtt", "auto")
            .build()
    }

    /**
     * rtext — список точек маршрута.
     *
     * Точки идут в порядке order:
     * lat1,lon1~lat2,lon2~lat3,lon3
     */
    private fun buildRouteText(
        sortedPoints: List<RoutePoint>
    ): String {
        return sortedPoints.joinToString(separator = "~") { point ->
            "${point.latitude},${point.longitude}"
        }
    }

    /**
     * Безопасный запуск intent.
     *
     * Если Android не нашёл приложение, ActivityNotFoundException
     * не уронит наше приложение.
     */
    private fun tryStartActivity(
        context: Context,
        intent: Intent
    ): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (exception: ActivityNotFoundException) {
            false
        } catch (exception: Exception) {
            false
        }
    }

    /**
     * Открывает страницу Яндекс Карт в Google Play.
     */
    private fun openYandexMapsInStore(context: Context) {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$YANDEX_MAPS_PACKAGE")
        )

        if (tryStartActivity(context, marketIntent)) {
            return
        }

        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$YANDEX_MAPS_PACKAGE")
        )

        tryStartActivity(context, webIntent)
    }

    private companion object {
        /**
         * Реальный package name Яндекс Карт.
         */
        const val YANDEX_MAPS_PACKAGE = "ru.yandex.yandexmaps"
    }
}