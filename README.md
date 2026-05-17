# TravelApp — приложение для совместного планирования путешествий

TravelApp — мобильное Android-приложение, разработанное в рамках выпускной квалификационной работы.

Приложение предназначено для совместного планирования поездок: пользователь может создавать поездки, добавлять точки маршрута через поиск мест, просматривать маршрут на карте, вести бюджет, приглашать участников и работать с поездкой совместно с другими пользователями.

## Основная идея проекта

Цель приложения — объединить в одном мобильном интерфейсе основные функции, необходимые для планирования путешествия:

- создание поездки;
- составление маршрута;
- поиск мест;
- отображение маршрута на карте;
- изменение порядка точек маршрута;
- экспорт маршрута во внешнее приложение Яндекс Карты;
- ведение бюджета поездки;
- приглашение участников;
- разграничение прав по ролям;
- уведомления о действиях внутри приложения.

Проект представляет собой прототип мобильного приложения, демонстрирующий основные сценарии совместного планирования путешествий.

---

## Возможности приложения

### Авторизация

В приложении реализованы:

- регистрация пользователя;
- вход по email и паролю;
- выход из аккаунта;
- отображение данных текущего пользователя в профиле.

Для авторизации используется Firebase Authentication.

---

### Работа с поездками

Пользователь может:

- создать новую поездку;
- указать название, описание и даты поездки;
- просмотреть список своих поездок;
- открыть выбранную поездку;
- удалить поездку, если он является организатором.

Поездки сохраняются в Cloud Firestore и доступны после повторного входа в приложение.

---

### Маршрут поездки

Во вкладке маршрута реализованы следующие возможности:

- поиск мест через Яндекс MapKit Search;
- отображение найденных вариантов;
- выбор найденного места;
- добавление места в маршрут поездки;
- сохранение координат, названия и адреса точки;
- добавление заметки к точке маршрута;
- изменение порядка точек маршрута;
- удаление точек маршрута.

Каждая точка маршрута имеет поле `order`, которое определяет порядок посещения мест.

---

### Карта

Во вкладке карты реализовано:

- отображение Яндекс.Карты;
- отображение маркеров точек маршрута;
- отображение линии между точками маршрута;
- построение линии в порядке, заданном пользователем;
- экспорт маршрута во внешнее приложение Яндекс Карты.

Внутри приложения отображается план маршрута, а внешний сервис Яндекс Карты используется для построения реального маршрута по дорожной сети.

---

### Бюджет поездки

Во вкладке бюджета пользователь может:

- добавить расход;
- указать название расхода;
- указать категорию расхода;
- указать сумму;
- просмотреть список расходов;
- удалить расход;
- увидеть общую сумму расходов по поездке.

---

### Участники поездки

В приложении реализована система совместного доступа к поездке.

Организатор может пригласить другого пользователя по email. Приглашённый пользователь после входа в приложение видит входящее приглашение и может принять или отклонить его.

После принятия приглашения поездка появляется в списке поездок приглашённого пользователя.

---

### Роли пользователей

В приложении используются роли участников поездки:

| Роль | Возможности |
|---|---|
| `ORGANIZER` | Полное управление поездкой, приглашение участников, редактирование маршрута и бюджета, удаление поездки |
| `EDITOR` | Редактирование маршрута и бюджета |
| `VIEWER` | Только просмотр данных поездки |

Ограничения реализованы на уровне пользовательского интерфейса.

---

### Приглашения

Механизм приглашений работает следующим образом:

1. Организатор вводит email участника.
2. В Firestore создаётся приглашение.
3. Приглашённый пользователь входит в приложение под этим email.
4. Пользователь открывает экран входящих приглашений.
5. Пользователь принимает приглашение.
6. Его `userId` добавляется в список участников поездки.
7. Поездка появляется в списке поездок пользователя.

---

### Уведомления

В приложении реализованы внутренние уведомления.

Уведомления создаются при действиях пользователя, например:

- добавлена точка маршрута;
- добавлен расход;
- приглашён участник.

Уведомления сохраняются в Firestore и отображаются на отдельном экране.

---

## Технологии

В проекте используются:

- Kotlin
- Android SDK
- Jetpack Compose
- MVVM
- Kotlin Coroutines
- StateFlow
- Firebase Authentication
- Cloud Firestore
- Yandex MapKit
- Yandex MapKit Search
- Git
- GitHub

---

## Архитектура

Проект построен по архитектурному подходу MVVM.

Общая схема приложения:

```text
UI / Jetpack Compose
↓
ViewModel
↓
Repository
↓
Firebase / Yandex MapKit
```

### UI-слой

UI реализован на Jetpack Compose.

Экраны приложения отвечают только за отображение данных и передачу действий пользователя во ViewModel.

Примеры экранов:

- `LoginScreen`
- `RegisterScreen`
- `TripsScreen`
- `CreateTripScreen`
- `TripScreen`
- `RouteTab`
- `MapTab`
- `BudgetTab`
- `ParticipantsTab`
- `InvitationsScreen`
- `NotificationsScreen`
- `ProfileScreen`

---

### ViewModel-слой

ViewModel отвечает за состояние экранов и обработку пользовательских действий.

Примеры ViewModel:

- `AuthViewModel`
- `TripsViewModel`
- `CreateTripViewModel`
- `TripViewModel`
- `RouteViewModel`
- `BudgetViewModel`
- `ParticipantsViewModel`
- `InvitationsViewModel`
- `NotificationsViewModel`
- `ProfileViewModel`

Для хранения состояния используются `StateFlow` и отдельные классы состояния экранов:

- `AuthUiState`
- `TripsUiState`
- `CreateTripUiState`
- `TripUiState`
- `RouteUiState`
- `BudgetUiState`
- `ParticipantsUiState`
- `InvitationsUiState`
- `NotificationsUiState`
- `ProfileUiState`

---

### Repository-слой

Repository скрывает источник данных от ViewModel.

ViewModel не обращается к Firebase или Яндекс MapKit напрямую. Вместо этого она работает через интерфейсы репозиториев.

Интерфейсы репозиториев:

- `AuthRepository`
- `TripRepository`
- `RouteRepository`
- `ExpenseRepository`
- `ParticipantRepository`
- `InvitationRepository`
- `NotificationRepository`
- `PlaceSearchRepository`

Реализации репозиториев:

- `FirebaseAuthRepository`
- `FirebaseTripRepository`
- `FirebaseRouteRepository`
- `FirebaseExpenseRepository`
- `FirebaseParticipantRepository`
- `FirebaseInvitationRepository`
- `FirebaseNotificationRepository`
- `YandexPlaceSearchRepository`

Такое разделение позволяет отделить бизнес-логику приложения от конкретных источников данных.

---

## Основные модели данных

### `User`

Модель пользователя приложения.

```kotlin
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = ""
)
```

---

### `Trip`

Модель поездки.

```kotlin
data class Trip(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: TripStatus = TripStatus.PLANNING,
    val ownerId: String = "",
    val participants: List<String> = emptyList()
)
```

---

### `RoutePoint`

Модель точки маршрута.

```kotlin
data class RoutePoint(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val address: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val order: Int = 0
)
```

---

### `Expense`

Модель расхода.

```kotlin
data class Expense(
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: Double = 0.0,
    val userId: String = "",
    val date: String = ""
)
```

---

### `TripParticipant`

Модель участника поездки.

```kotlin
data class TripParticipant(
    val id: String = "",
    val tripId: String = "",
    val email: String = "",
    val role: ParticipantRole = ParticipantRole.VIEWER,
    val status: ParticipantStatus = ParticipantStatus.INVITED
)
```

---

### `TripInvitation`

Модель приглашения в поездку.

```kotlin
data class TripInvitation(
    val id: String = "",
    val tripId: String = "",
    val tripTitle: String = "",
    val inviterUserId: String = "",
    val inviteeEmail: String = "",
    val role: ParticipantRole = ParticipantRole.VIEWER,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: String = ""
)
```

---

### `NotificationItem`

Модель внутреннего уведомления.

```kotlin
data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val tripId: String = "",
    val text: String = "",
    val createdAt: String = ""
)
```

---

### `PlaceSearchResult`

Модель результата поиска места.

```kotlin
data class PlaceSearchResult(
    val title: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
```

---

## Структура проекта

Примерная структура проекта:

```text
com.example.travelapp/

    MainActivity.kt
    TravelAppApplication.kt

    core/
        AppResult.kt
        ViewModelFactory.kt

    data/
        model/
            User.kt
            Trip.kt
            TripStatus.kt
            RoutePoint.kt
            Expense.kt
            ExpenseCategory.kt
            TripParticipant.kt
            ParticipantRole.kt
            ParticipantStatus.kt
            TripInvitation.kt
            InvitationStatus.kt
            NotificationItem.kt
            PlaceSearchResult.kt

        repository/
            AuthRepository.kt
            TripRepository.kt
            RouteRepository.kt
            ExpenseRepository.kt
            ParticipantRepository.kt
            InvitationRepository.kt
            NotificationRepository.kt
            PlaceSearchRepository.kt

        repository/impl/
            FirebaseAuthRepository.kt
            FirebaseTripRepository.kt
            FirebaseRouteRepository.kt
            FirebaseExpenseRepository.kt
            FirebaseParticipantRepository.kt
            FirebaseInvitationRepository.kt
            FirebaseNotificationRepository.kt
            YandexPlaceSearchRepository.kt

    presentation/
        navigation/
            AppNavigation.kt
            Screen.kt

        auth/
            LoginScreen.kt
            RegisterScreen.kt
            AuthViewModel.kt
            AuthUiState.kt

        trips/
            TripsScreen.kt
            CreateTripScreen.kt
            TripsViewModel.kt
            CreateTripViewModel.kt
            TripsUiState.kt
            CreateTripUiState.kt

        trip/
            TripScreen.kt
            TripViewModel.kt
            TripUiState.kt

        route/
            RouteTab.kt
            RouteViewModel.kt
            RouteUiState.kt

        map/
            MapTab.kt
            YandexMapsExporter.kt

        budget/
            BudgetTab.kt
            BudgetViewModel.kt
            BudgetUiState.kt

        participants/
            ParticipantsTab.kt
            ParticipantsViewModel.kt
            ParticipantsUiState.kt

        invitations/
            InvitationsScreen.kt
            InvitationsViewModel.kt
            InvitationsUiState.kt

        notifications/
            NotificationsScreen.kt
            NotificationsViewModel.kt
            NotificationsUiState.kt

        profile/
            ProfileScreen.kt
            ProfileViewModel.kt
            ProfileUiState.kt

    ui/
        theme/
```

---

## Структура Firestore

Примерная структура базы данных Cloud Firestore:

```text
users/
    userId/
        id
        email
        name

trips/
    tripId/
        id
        title
        description
        startDate
        endDate
        status
        ownerId
        participants

        routePoints/
            pointId/
                id
                tripId
                title
                address
                description
                latitude
                longitude
                order

        expenses/
            expenseId/
                id
                tripId
                title
                category
                amount
                userId
                date

        participants/
            participantId/
                id
                tripId
                email
                role
                status

invitations/
    invitationId/
        id
        tripId
        tripTitle
        inviterUserId
        inviteeEmail
        role
        status
        createdAt

notifications/
    notificationId/
        id
        userId
        tripId
        text
        createdAt
```

---

## Логика отображения поездок

Список поездок пользователя загружается по массиву `participants`.

Если `userId` текущего пользователя находится в массиве `participants`, поездка отображается в списке пользователя.

Это позволяет реализовать совместный доступ к одной поездке для нескольких пользователей.

---

## Логика приглашений

Приглашения хранятся отдельно от поездок в коллекции `invitations`.

Когда пользователь принимает приглашение:

1. статус приглашения меняется на `ACCEPTED`;
2. `userId` пользователя добавляется в массив `participants` у поездки;
3. пользователь добавляется в подколлекцию `participants` внутри поездки;
4. поездка появляется в списке поездок приглашённого пользователя.

---

## Логика маршрута

Маршрут поездки состоит из точек `RoutePoint`.

Каждая точка содержит:

- название;
- адрес;
- координаты;
- описание;
- порядковый номер.

Порядок точек определяется полем `order`.

Пользователь может изменить порядок точек с помощью кнопок вверх и вниз. После изменения порядка новые значения `order` сохраняются в Firestore.

---

## Логика карты

Во вкладке карты отображаются:

- маркеры точек маршрута;
- линия между точками маршрута;
- кнопка экспорта маршрута в Яндекс Карты.

Внутри приложения линия между точками используется для визуального отображения порядка маршрута. Для построения реального маршрута по дорогам используется экспорт во внешнее приложение Яндекс Карты.

---

## Экспорт маршрута в Яндекс Карты

Маршрут экспортируется во внешнее приложение Яндекс Карты.

Точки маршрута передаются в порядке поля `order`.

Сценарий:

1. пользователь формирует маршрут в приложении;
2. нажимает кнопку открытия маршрута в Яндекс Картах;
3. приложение передаёт координаты точек во внешнее приложение;
4. Яндекс Карты строят маршрут по дорожной сети.

---

## Настройка проекта

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd TravelApp
```

---

### 2. Firebase

Для работы приложения необходимо создать проект Firebase.

В Firebase нужно включить:

- Firebase Authentication;
- Email/Password Sign-in;
- Cloud Firestore.

Файл конфигурации Firebase:

```text
google-services.json
```

должен находиться по пути:

```text
app/google-services.json
```

---

### 3. Яндекс MapKit API Key

Для работы карты и поиска мест необходимо получить API-ключ Яндекс MapKit.

В корне проекта нужно создать или открыть файл:

```text
local.properties
```

И добавить туда строку:

```properties
MAPKIT_API_KEY=your_yandex_mapkit_api_key
```

Файл `local.properties` не должен попадать в репозиторий.

---

### 4. Firestore Rules для разработки

Для разработки можно использовать следующие правила:

```text
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Эти правила подходят для тестирования прототипа. Для production-версии необходимо добавить более строгую проверку прав, например проверку того, что пользователь является участником конкретной поездки.

---

## Сборка и запуск

Для запуска проекта необходимо:

1. открыть проект в Android Studio;
2. добавить `google-services.json` в папку `app`;
3. добавить `MAPKIT_API_KEY` в `local.properties`;
4. выполнить Gradle Sync;
5. запустить приложение на Android-устройстве или эмуляторе.

Для проверки экспорта маршрута желательно использовать реальное Android-устройство с установленным приложением Яндекс Карты.

---

## Git workflow

В проекте используются две основные ветки:

```text
develop — ветка разработки
master  — стабильная версия проекта
```

Рабочий процесс:

```bash
git checkout develop
git add .
git commit -m "Commit message"
git push origin develop
```

После проверки стабильной версии:

```bash
git checkout master
git pull origin master
git merge develop
git push origin master
```

---

## Демонстрационный сценарий

Основной сценарий демонстрации приложения:

1. Пользователь регистрируется или входит в аккаунт.
2. Создаёт новую поездку.
3. Открывает созданную поездку.
4. Переходит во вкладку маршрута.
5. Выполняет поиск места.
6. Добавляет найденное место в маршрут.
7. Добавляет ещё несколько точек маршрута.
8. Меняет порядок точек маршрута.
9. Открывает вкладку карты.
10. Проверяет отображение маркеров и линии маршрута.
11. Экспортирует маршрут в Яндекс Карты.
12. Переходит во вкладку бюджета.
13. Добавляет расходы.
14. Проверяет общую сумму расходов.
15. Переходит во вкладку участников.
16. Приглашает второго пользователя по email.
17. Второй пользователь входит в приложение.
18. Второй пользователь принимает приглашение.
19. Поездка появляется у второго пользователя.
20. Пользователь просматривает уведомления.
21. Пользователь открывает профиль.
22. Организатор удаляет поездку.

---

## Ограничения текущей версии

Текущая версия является прототипом и имеет ряд ограничений:

- ограничения ролей реализованы на уровне интерфейса;
- правила Firestore для production-версии требуют доработки;
- линия маршрута внутри приложения отображает порядок точек, но не является полноценным дорожным маршрутом;
- построение реального маршрута выполняется через экспорт в Яндекс Карты;
- push-уведомления не реализованы, используются внутренние уведомления приложения;
- отсутствует офлайн-режим;
- отсутствует загрузка фотографий к поездке.

---

## Возможные направления развития

В дальнейшем приложение можно расширить следующими функциями:

- более строгие Firestore Security Rules;
- push-уведомления через Firebase Cloud Messaging;
- оффлайн-режим;
- загрузка фотографий поездки;
- автоматическая оптимизация порядка точек маршрута;
- построение дорожного маршрута внутри приложения;
- экспорт маршрута в другие картографические сервисы;
- расширенная аналитика бюджета;
- распределение расходов между участниками;
- редактирование данных поездки;
- редактирование профиля пользователя.

---

## Назначение проекта

Проект разработан в рамках выпускной квалификационной работы и предназначен для демонстрации концепции мобильного приложения для совместного планирования путешествий.

Основная задача приложения — показать, как с помощью современных мобильных технологий можно объединить планирование маршрута, работу с картой, управление бюджетом и совместный доступ к поездке в одном Android-приложении.

---

## Автор

Разработчик: Денис Яргин

Тип проекта: выпускная квалификационная работа  
Платформа: Android  
Год: 2026
