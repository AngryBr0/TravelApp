package com.example.travelapp.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.ui.components.AppMutedText
import com.example.travelapp.ui.theme.TravelAppTheme
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlin.math.max
import com.example.travelapp.ui.components.AppBottomActionButton

/**
 * MapTab — вкладка карты маршрута.
 *
 * Теперь карта отображает:
 * - маркеры с номерами;
 * - названия точек рядом с маркерами;
 * - синюю линию маршрута;
 * - компактную панель маршрута снизу.
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView
            },
            update = { view ->
                val map = view.mapWindow.map
                val mapObjects = map.mapObjects

                mapObjects.clear()

                /**
                 * Добавляем маркеры с номерами и подписями.
                 */
                sortedPoints.forEachIndexed { index, routePoint ->
                    val markerBitmap = createNumberedMarkerBitmap(
                        context = context,
                        number = index + 1
                    )

                    val markerIcon = ImageProvider.fromBitmap(markerBitmap)

                    val placemark = mapObjects.addPlacemark()
                    placemark.geometry = Point(
                        routePoint.latitude,
                        routePoint.longitude
                    )
                    placemark.setIcon(markerIcon)
                }

                /**
                 * Рисуем линию маршрута в порядке order.
                 */
                val polylinePoints = sortedPoints.map { routePoint ->
                    Point(routePoint.latitude, routePoint.longitude)
                }

                if (polylinePoints.size >= 2) {
                    val polyline = mapObjects.addPolyline(
                        Polyline(polylinePoints)
                    )

                    polyline.setStrokeColor(
                        AndroidColor.rgb(37, 99, 235)
                    )
                    polyline.setStrokeWidth(5.0f)
                }

                moveCameraToRoute(
                    map = map,
                    points = sortedPoints
                )
            }
        )

        RouteMapBottomPanel(
            points = sortedPoints,
            onOpenInYandexMapsClick = {
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
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        )
    }
}

/**
 * Плавающая кнопка экспорта маршрута в Яндекс Карты.
 */
@Composable
private fun RouteMapBottomPanel(
    points: List<RoutePoint>,
    onOpenInYandexMapsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppBottomActionButton(
        text = "Экспорт в Яндекс Карты",
        icon = null,
        onClick = onOpenInYandexMapsClick,
        enabled = points.size >= 2,
        modifier = modifier,
        width = 260.dp
    )
}

/**
 * Создаёт круглую иконку маркера с номером.
 *
 * Вместо одинаковых красных пинов пользователь видит:
 * 1, 2, 3...
 * Это сразу показывает порядок маршрута.
 */
private fun createNumberedMarkerBitmap(
    context: Context,
    number: Int
): Bitmap {
    val density = context.resources.displayMetrics.density

    val size = (42 * density).toInt()
    val radius = size * 0.38f

    val bitmap = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(37, 99, 235)
        style = Paint.Style.FILL
    }

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = 16f * density
    }

    val centerX = size / 2f
    val centerY = size / 2f

    canvas.drawCircle(
        centerX,
        centerY,
        radius,
        circlePaint
    )

    canvas.drawCircle(
        centerX,
        centerY,
        radius,
        borderPaint
    )

    val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2

    canvas.drawText(
        number.toString(),
        centerX,
        textY,
        textPaint
    )

    return bitmap
}

/**
 * Двигает камеру так, чтобы было видно весь маршрут.
 *
 * Это не идеальный fitBounds, но для дипломного прототипа
 * работает намного лучше, чем фокус только на первой точке.
 */
private fun moveCameraToRoute(
    map: Map,
    points: List<RoutePoint>
) {
    if (points.isEmpty()) {
        map.move(
            CameraPosition(
                Point(55.751225, 37.62954),
                10.0f,
                0.0f,
                0.0f
            )
        )
        return
    }

    val minLat = points.minOf { point ->
        point.latitude
    }

    val maxLat = points.maxOf { point ->
        point.latitude
    }

    val minLon = points.minOf { point ->
        point.longitude
    }

    val maxLon = points.maxOf { point ->
        point.longitude
    }

    val centerLat = (minLat + maxLat) / 2.0
    val centerLon = (minLon + maxLon) / 2.0

    val delta = max(
        maxLat - minLat,
        maxLon - minLon
    )

    val zoom = when {
        delta < 0.005 -> 16.0f
        delta < 0.02 -> 14.0f
        delta < 0.08 -> 12.0f
        delta < 0.3 -> 10.0f
        delta < 1.0 -> 8.0f
        else -> 5.0f
    }

    map.move(
        CameraPosition(
            Point(centerLat, centerLon),
            zoom,
            0.0f,
            0.0f
        )
    )
}




/**
 * Preview без настоящего MapView.
 */
@Composable
private fun MapTabPreviewContent(
    routePoints: List<RoutePoint>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Здесь будет Яндекс.Карта",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AppMutedText(
                text = "В Preview карта заменена заглушкой"
            )
        }

        RouteMapBottomPanel(
            points = routePoints,
            onOpenInYandexMapsClick = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        )
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
                ),
                RoutePoint(
                    id = "3",
                    tripId = "trip-1",
                    title = "Исаакиевский собор",
                    address = "Санкт-Петербург",
                    description = "",
                    latitude = 59.9340,
                    longitude = 30.3061,
                    order = 3
                )
            )
        )
    }
}