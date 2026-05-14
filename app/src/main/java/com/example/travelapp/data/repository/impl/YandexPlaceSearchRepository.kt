package com.example.travelapp.data.repository.impl

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.PlaceSearchResult
import com.example.travelapp.data.repository.PlaceSearchRepository
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * YandexPlaceSearchRepository — реализация поиска мест через Яндекс MapKit Search.
 *
 * Этот класс отвечает только за поиск мест.
 * Он не сохраняет точки маршрута в Firestore — этим занимается RouteRepository.
 */
class YandexPlaceSearchRepository : PlaceSearchRepository {

    /**
     * SearchManager выполняет поисковые запросы.
     *
     * SearchManagerType.COMBINED означает комбинированный поиск:
     * по организациям, адресам и другим объектам.
     */
    private val searchManager: SearchManager =
        SearchFactory.getInstance()
            .createSearchManager(SearchManagerType.COMBINED)

    /**
     * Храним текущую поисковую сессию.
     *
     * Методы SearchManager возвращают Session.
     * Session позволяет отменить запрос через cancel().
     */
    private var searchSession: Session? = null

    /**
     * Выполняет поиск мест по текстовому запросу.
     *
     * Например:
     * "Эрмитаж"
     * "Казанский собор"
     * "Красная площадь"
     */
    override suspend fun searchPlaces(
        query: String
    ): AppResult<List<PlaceSearchResult>> {
        if (query.isBlank()) {
            return AppResult.Error("Введите название места")
        }

        return suspendCancellableCoroutine { continuation ->

            /**
             * Область поиска.
             *
             * Для прототипа задаём большую область,
             * охватывающую Россию и часть соседних регионов.
             */
            val searchGeometry = Geometry.fromBoundingBox(
                BoundingBox(
                    Point(41.0, 19.0),
                    Point(82.0, 180.0)
                )
            )

            val searchOptions = SearchOptions().apply {
                resultPageSize = 10
            }

            /**
             * submit запускает поисковый запрос.
             *
             * В этой версии SDK listener имеет тип Session.SearchListener,
             * поэтому используем именно object : Session.SearchListener.
             */
            searchSession = searchManager.submit(
                query,
                searchGeometry,
                searchOptions,
                object : Session.SearchListener {

                    /**
                     * Вызывается, когда Яндекс вернул результаты поиска.
                     */
                    override fun onSearchResponse(response: Response) {
                        val places = response.collection.children
                            .mapNotNull { item ->
                                val geoObject = item.obj ?: return@mapNotNull null

                                val point = geoObject.geometry
                                    .firstOrNull()
                                    ?.point
                                    ?: return@mapNotNull null

                                PlaceSearchResult(
                                    title = geoObject.name.orEmpty(),
                                    address = geoObject.descriptionText.orEmpty(),
                                    latitude = point.latitude,
                                    longitude = point.longitude
                                )
                            }
                            .filter { place ->
                                place.title.isNotBlank()
                            }

                        continuation.resume(
                            AppResult.Success(places)
                        )
                    }

                    /**
                     * Вызывается, если поиск завершился ошибкой.
                     */
                    override fun onSearchError(error: Error) {
                        continuation.resume(
                            AppResult.Error("Ошибка поиска места")
                        )
                    }
                }
            )

            /**
             * Если корутина отменится, отменяем и поисковый запрос.
             */
            continuation.invokeOnCancellation {
                searchSession?.cancel()
            }
        }
    }
}