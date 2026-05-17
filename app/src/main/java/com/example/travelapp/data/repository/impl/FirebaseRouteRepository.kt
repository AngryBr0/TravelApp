package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.RoutePoint
import com.example.travelapp.data.repository.RouteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseRouteRepository — реальная реализация RouteRepository через Firestore.
 *
 * Этот класс отвечает за работу с точками маршрута конкретной поездки.
 *
 * Структура хранения:
 *
 * trips/
 *   tripId/
 *     routePoints/
 *       pointId/
 *         id
 *         tripId
 *         title
 *         address
 *         description
 *         latitude
 *         longitude
 *         order
 *          dayNumber
 *
 * ViewModel не знает, что данные хранятся в Firestore.
 * Она работает только через интерфейс RouteRepository.
 */
class FirebaseRouteRepository(
    private val firestore: FirebaseFirestore
) : RouteRepository {

    /**
     * Возвращает ссылку на подколлекцию routePoints конкретной поездки.
     *
     * tripId — id выбранной поездки.
     */
    private fun routePointsCollection(tripId: String) =
        firestore
            .collection("trips")
            .document(tripId)
            .collection("routePoints")

    /**
     * Добавляет точку маршрута в Firestore.
     */
    override suspend fun addRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank()) {
                return AppResult.Error("Не указан id поездки")
            }

            if (point.title.isBlank()) {
                return AppResult.Error("Введите название точки маршрута")
            }
            /**
             * document() без параметров создаёт новый документ
             * с автоматически сгенерированным id.
             */
            val document = routePointsCollection(tripId).document()

            val pointWithId = point.copy(
                id = document.id,
                tripId = tripId
            )

            /**
             * Сохраняем данные как Map.
             *
             * Это удобно, потому что мы точно контролируем,
             * какие поля попадут в Firestore.
             */
            val pointMap = mapOf(
                "id" to pointWithId.id,
                "tripId" to pointWithId.tripId,
                "title" to pointWithId.title,
                "address" to pointWithId.address,
                "description" to pointWithId.description,
                "latitude" to pointWithId.latitude,
                "longitude" to pointWithId.longitude,
                "order" to pointWithId.order,
                "dayNumber" to pointWithId.dayNumber
            )

            document.set(pointMap).await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка добавления точки маршрута"
            )
        }
    }

    /**
     * Подписывается на список точек маршрута конкретной поездки.
     *
     * callbackFlow нужен, чтобы превратить Firestore listener
     * в Kotlin Flow.
     *
     * Когда в Firestore добавляется или удаляется точка,
     * экран автоматически получает обновлённый список.
     */
    override fun observeRoutePoints(
        tripId: String
    ): Flow<AppResult<List<RoutePoint>>> = callbackFlow {
        trySend(AppResult.Loading)

        val listener = routePointsCollection(tripId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(
                        AppResult.Error(
                            error.message ?: "Ошибка загрузки маршрута"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(AppResult.Success(emptyList()))
                    return@addSnapshotListener
                }

                val points = snapshot.documents
                    .mapNotNull { document ->
                        document.toRoutePointOrNull()
                    }
                    .sortedWith(
                        compareBy<RoutePoint> { point ->
                            point.dayNumber
                        }.thenBy { point ->
                            point.order
                        }
                    )

                trySend(AppResult.Success(points))
            }

        /**
         * Когда Flow больше не нужен,
         * удаляем listener, чтобы не было утечек памяти.
         */
        awaitClose {
            listener.remove()
        }
    }
    /**
     * Обновляет порядок точек маршрута в Firestore.
     *
     * Используется batch, чтобы все order обновились одним набором операций.
     * Это важно, потому что при перестановке двух точек нужно сохранить
     * новый порядок сразу у нескольких документов.
     */
    override suspend fun updateRoutePointsOrder(
        tripId: String,
        points: List<RoutePoint>
    ): AppResult<Unit> {
        return try {
            val batch = firestore.batch()

            points.forEach { point ->
                val document = routePointsCollection(tripId)
                    .document(point.id)

                batch.update(
                    document,
                    mapOf(
                        "order" to point.order,
                        "dayNumber" to point.dayNumber
                    )
                )
            }

            batch.commit().await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка изменения порядка маршрута"
            )
        }
    }

    /**
     * Обновляет данные точки маршрута.
     *
     * Координаты и адрес не трогаем, потому что они получены из поиска Яндекса.
     * Пользователь редактирует только название и заметку.
     */
    override suspend fun updateRoutePoint(
        tripId: String,
        point: RoutePoint
    ): AppResult<Unit> {
        return try {
            if (point.title.isBlank()) {
                return AppResult.Error("Введите название точки")
            }

            routePointsCollection(tripId)
                .document(point.id)
                .update(
                    mapOf(
                        "title" to point.title.trim(),
                        "description" to point.description.trim()
                    )
                )
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка обновления точки маршрута"
            )
        }
    }

    /**
     * Удаляет точку маршрута из Firestore.
     */
    override suspend fun deleteRoutePoint(
        tripId: String,
        pointId: String
    ): AppResult<Unit> {
        return try {
            if (tripId.isBlank() || pointId.isBlank()) {
                return AppResult.Error("Не указан id точки маршрута")
            }

            routePointsCollection(tripId)
                .document(pointId)
                .delete()
                .await()

            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Error(
                exception.message ?: "Ошибка удаления точки маршрута"
            )
        }
    }

    /**
     * Преобразует документ Firestore в объект RoutePoint.
     *
     * Если в документе нет обязательного title,
     * возвращаем null и не показываем такой объект в списке.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toRoutePointOrNull(): RoutePoint? {
        val id = getString("id") ?: this.id
        val title = getString("title") ?: return null

        return RoutePoint(
            id = id,
            tripId = getString("tripId").orEmpty(),
            title = title,
            address = getString("address").orEmpty(),
            description = getString("description").orEmpty(),
            latitude = getDouble("latitude") ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            order = getLong("order")?.toInt() ?: 1,
            dayNumber = getLong("dayNumber")?.toInt() ?: 1
        )
    }
}