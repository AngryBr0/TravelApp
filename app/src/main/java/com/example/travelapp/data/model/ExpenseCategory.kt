package com.example.travelapp.data.model

/**
 * ExpenseCategory — категории расходов.
 *
 * displayName используется для красивого отображения в интерфейсе.
 * name используется для сохранения в Firestore.
 */
enum class ExpenseCategory(
    val displayName: String
) {
    TRANSPORT("Транспорт"),
    TAXI("Такси"),
    FLIGHTS("Авиабилеты"),
    TRAIN("Ж/д билеты"),
    FUEL("Топливо"),
    PARKING("Парковка"),

    FOOD("Еда"),
    CAFE("Кафе и рестораны"),
    GROCERIES("Продукты"),

    HOTEL("Проживание"),
    RENT("Аренда жилья"),

    ENTERTAINMENT("Развлечения"),
    MUSEUMS("Музеи"),
    EXCURSIONS("Экскурсии"),
    TICKETS("Билеты"),

    SHOPPING("Покупки"),
    SOUVENIRS("Сувениры"),

    MEDICINE("Аптека"),
    INSURANCE("Страховка"),
    COMMUNICATION("Связь и интернет"),

    OTHER("Другое");

    companion object {
        fun fromText(text: String): ExpenseCategory {
            val normalized = text.trim().lowercase()

            return entries.firstOrNull { category ->
                category.displayName.lowercase() == normalized ||
                        category.name.lowercase() == normalized
            } ?: OTHER
        }
    }
}