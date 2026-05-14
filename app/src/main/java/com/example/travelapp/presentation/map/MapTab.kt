package com.example.travelapp.presentation.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travelapp.R
import com.example.travelapp.data.model.RoutePoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.yandex.mapkit.geometry.Polyline
import androidx.compose.material3.Button
/**
 * MapTab — вкладка карты.
 *
 * Здесь отображается Яндекс.Карта и маркеры точек маршрута.
 *
 * Важно:
 * MapKit — это View-based SDK, поэтому внутри Compose
 * мы используем AndroidView, чтобы встроить обычный Android View.
 */
@Composable
fun MapTab(
    tripId: String,
    routePoints: List<RoutePoint>
) {
    val context = LocalContext.current

    /**
     * Создаем MapView один раз и запоминаем его.
     *
     * remember нужен, чтобы карта не пересоздавалась
     * при каждой перерисовке Compose.
     */
    val mapView = remember {
        MapView(context)
    }

    /**
     * Передаем жизненный цикл в MapView.
     *
     * Когда вкладка появляется — вызываем onStart().
     * Когда уходит с экрана — вызываем onStop().
     */
    DisposableEffect(Unit) {
        mapView.onStart()

        onDispose {
            mapView.onStop()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Карта маршрута",
            modifier = Modifier.padding(16.dp)
        )
        val exporter = remember { YandexNavigatorExporter() }

        Button(
            onClick = {
                exporter.openRouteInYandexNavigator(
                    context = context,
                    routePoints = routePoints
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            enabled = routePoints.size >= 2
        ) {
            Text("Открыть маршрут в Яндекс Навигаторе")
        }

        Spacer(modifier = Modifier.height(12.dp))
        /**
         * Если точек маршрута нет, показываем подсказку.
         */
        if (routePoints.isEmpty()) {
            Text(
                text = "Добавьте точки маршрута, чтобы увидеть их на карте",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        /**
         * AndroidView позволяет встроить MapView внутрь Compose.
         */
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView
            },
            update = { view ->
                val map = view.mapWindow.map
                val mapObjects = map.mapObjects

                /**
                 * Очищаем старые маркеры перед добавлением новых.
                 * Иначе при обновлении списка точки будут дублироваться.
                 */
                mapObjects.clear()

                val validPoints = routePoints
                    .filter { point ->
                        point.latitude != 0.0 || point.longitude != 0.0
                    }
                    .sortedBy { point ->
                        point.order
                    }

                val imageProvider = ImageProvider.fromResource(
                    context,
                    R.drawable.ic_map_pin
                )

                /**
                 * Добавляем маркеры всех точек маршрута.
                 */
                validPoints.forEach { routePoint ->
                    mapObjects.addPlacemark(
                        Point(routePoint.latitude, routePoint.longitude),
                        imageProvider
                    )
                }
                /**
                 * Рисуем линию маршрута между точками
                 * в порядке их order.
                 */
                val polylinePoints = validPoints
                    .sortedBy { routePoint ->
                        routePoint.order
                    }
                    .map { routePoint ->
                        Point(routePoint.latitude, routePoint.longitude)
                    }

                if (polylinePoints.size >= 2) {
                    mapObjects.addPolyline(
                        Polyline(polylinePoints)
                    )
                }
                /**
                 * Если есть хотя бы одна точка,
                 * перемещаем камеру к первой точке маршрута.
                 */
                val firstPoint = validPoints.firstOrNull()

                if (firstPoint != null) {
                    map.move(
                        CameraPosition(
                            Point(firstPoint.latitude, firstPoint.longitude),
                            12.0f,
                            0.0f,
                            0.0f
                        )
                    )
                } else {
                    /**
                     * Если точек нет, показываем Москву как стартовую позицию.
                     */
                    map.move(
                        CameraPosition(
                            Point(55.751225, 37.62954),
                            10.0f,
                            0.0f,
                            0.0f
                        )
                    )
                }
            }
        )
    }
}