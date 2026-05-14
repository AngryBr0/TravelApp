package com.example.travelapp.data.repository

import com.example.travelapp.core.AppResult
import com.example.travelapp.data.model.PlaceSearchResult

/**
 * PlaceSearchRepository — интерфейс для поиска мест.
 *
 * ViewModel не должна знать, что поиск выполняется через Яндекс.
 * Она работает с абстракцией.
 */
interface PlaceSearchRepository {

    /**
     * Выполняет поиск мест по текстовому запросу.
     *
     * Например:
     * "Эрмитаж"
     * "Казанский собор"
     * "Красная площадь"
     */
    suspend fun searchPlaces(
        query: String
    ): AppResult<List<PlaceSearchResult>>
}