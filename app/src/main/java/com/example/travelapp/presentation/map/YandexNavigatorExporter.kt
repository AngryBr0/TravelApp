package com.example.travelapp.presentation.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.travelapp.data.model.RoutePoint

/**
 * YandexNavigatorExporter — класс для открытия маршрута
 * во внешнем приложении Яндекс Навигатор.
 *
 * Мы передаем точки маршрута в том порядке,
 * который задан в нашем приложении через поле order.
 */
class YandexNavigatorExporter {

    /**
     * Открывает маршрут в Яндекс Навигаторе.
     *
     * Первая точка становится начальной,
     * последняя — конечной,
     * все точки между ними — промежуточными.
     */
    fun openRouteInYandexNavigator(
        context: Context,
        routePoints: List<RoutePoint>
    ) {
        val sortedPoints = routePoints
            .filter { point ->
                point.latitude != 0.0 || point.longitude != 0.0
            }
            .sortedBy { point ->
                point.order
            }

        if (sortedPoints.size < 2) {
            return
        }

        val firstPoint = sortedPoints.first()
        val lastPoint = sortedPoints.last()
        val viaPoints = sortedPoints.drop(1).dropLast(1)

        val uriBuilder = Uri
            .parse("yandexnavi://build_route_on_map")
            .buildUpon()
            .appendQueryParameter("lat_from", firstPoint.latitude.toString())
            .appendQueryParameter("lon_from", firstPoint.longitude.toString())
            .appendQueryParameter("lat_to", lastPoint.latitude.toString())
            .appendQueryParameter("lon_to", lastPoint.longitude.toString())

        viaPoints.forEachIndexed { index, point ->
            uriBuilder.appendQueryParameter(
                "lat_via_$index",
                point.latitude.toString()
            )
            uriBuilder.appendQueryParameter(
                "lon_via_$index",
                point.longitude.toString()
            )
        }

        val intent = Intent(
            Intent.ACTION_VIEW,
            uriBuilder.build()
        ).apply {
            setPackage("ru.yandex.yandexnavi")
        }

        context.startActivity(intent)
    }
}