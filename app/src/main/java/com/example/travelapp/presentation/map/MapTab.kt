package com.example.travelapp.presentation.map

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import com.example.travelapp.R
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.ui.theme.TravelAppTheme
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

/**
 * MapTab — вкладка карты маршрута.
 *
 * Экспорт в Яндекс Карты теперь вынесен в компактную верхнюю панель,
 * а не лежит большой карточкой поверх карты.
 */
@Composable
fun MapTab(
    tripId: String,
    routePoints: List<RoutePoint>
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val sortedPoints = routePoints
        .filter { point ->
            point.latitude != 0.0 || point.longitude != 0.0
        }
        .sortedBy { point ->
            point.order
        }

    if (isPreview) {
        MapTabPreviewContent(routePoints = sortedPoints)
        return
    }

    val mapView = remember {
        MapView(context)
    }

    val exporter = remember {
        YandexMapsExporter()
    }

    DisposableEffect(Unit) {
        mapView.onStart()

        onDispose {
            mapView.onStop()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MapCompactHeader(
            pointsCount = sortedPoints.size,
            canOpenInMaps = sortedPoints.size >= 2,
            onOpenInMapsClick = {
                val isOpened = exporter.openRouteInYandexMaps(
                    context = context,
                    routePoints = sortedPoints
                )

                if (!isOpened) {
                    Toast.makeText(
                        context,
                        "Не удалось открыть маршрут в Яндекс Картах",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView
            },
            update = { view ->
                val map = view.mapWindow.map
                val mapObjects = map.mapObjects

                mapObjects.clear()

                val imageProvider = ImageProvider.fromResource(
                    context,
                    R.drawable.ic_map_pin
                )

                sortedPoints.forEach { routePoint ->
                    val placemark = mapObjects.addPlacemark()
                    placemark.geometry = Point(routePoint.latitude, routePoint.longitude)
                    placemark.setIcon(imageProvider)
                }

                val polylinePoints = sortedPoints.map { routePoint ->
                    Point(routePoint.latitude, routePoint.longitude)
                }

                if (polylinePoints.size >= 2) {
                    mapObjects.addPolyline(
                        Polyline(polylinePoints)
                    )
                }

                val firstPoint = sortedPoints.firstOrNull()

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

/**
 * Компактная панель над картой.
 */
@Composable
private fun MapCompactHeader(
    pointsCount: Int,
    canOpenInMaps: Boolean,
    onOpenInMapsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Карта маршрута",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = when (pointsCount) {
                    0 -> "Нет точек"
                    1 -> "1 точка"
                    else -> "$pointsCount точки"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onOpenInMapsClick,
            enabled = canOpenInMaps,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF2563EB)
            )
        ) {
            Text(
                text = "Открыть",
                maxLines = 1
            )
        }
    }
}

/**
 * Preview без настоящего MapView.
 */
@Composable
private fun MapTabPreviewContent(
    routePoints: List<RoutePoint>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MapCompactHeader(
            pointsCount = routePoints.size,
            canOpenInMaps = routePoints.size >= 2,
            onOpenInMapsClick = {}
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Здесь будет Яндекс.Карта",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapTabPreview() {
    TravelAppTheme {
        MapTab(
            tripId = "trip-1",
            routePoints = listOf(
                RoutePoint(
                    id = "1",
                    tripId = "trip-1",
                    title = "Эрмитаж",
                    address = "Санкт-Петербург",
                    description = "",
                    latitude = 59.9398,
                    longitude = 30.3146,
                    order = 1
                ),
                RoutePoint(
                    id = "2",
                    tripId = "trip-1",
                    title = "Казанский собор",
                    address = "Санкт-Петербург",
                    description = "",
                    latitude = 59.9343,
                    longitude = 30.3245,
                    order = 2
                )
            )
        )
    }
}