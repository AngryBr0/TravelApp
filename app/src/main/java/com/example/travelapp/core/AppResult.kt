package com.example.travelapp.core

/**
 * AppResult — общий тип результата операции.
 *
 * Он нужен, чтобы все операции в приложении возвращали результат
 * в едином виде: успех, ошибка или загрузка.
 *
 * Например:
 * - успешно получили список поездок — Success
 * - произошла ошибка Firebase — Error
 * - данные загружаются — Loading
 */
sealed class AppResult<out T> {

    /**
     * Успешный результат.
     *
     * data хранит данные, которые вернула операция.
     * Например, список поездок.
     */
    data class Success<T>(val data: T) : AppResult<T>()

    /**
     * Ошибка.
     *
     * message содержит текст ошибки, который можно показать пользователю.
     */
    data class Error(val message: String) : AppResult<Nothing>()

    /**
     * Состояние загрузки.
     *
     * Используется, когда данные еще загружаются.
     */
    data object Loading : AppResult<Nothing>()
}