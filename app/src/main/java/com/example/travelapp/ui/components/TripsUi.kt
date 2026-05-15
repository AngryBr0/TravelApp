package com.example.travelapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon



private val AppBlue = Color(0xFF2563EB)
private val AppBackground = Color(0xFFFAFAF7)
private val AppDarkText = Color(0xFF111827)
private val AppMutedText = Color(0xFF6B7280)
private val CardBorder = Color(0xFFE5E7EB)

/**
 * Scaffold главного экрана.
 *
 * Верхняя кнопка уведомлений убрана, потому что уведомления уже есть
 * в нижнем меню. Слева также нет нерабочего аватара.
 */
/**
 * Общий Scaffold для главных разделов приложения:
 * Поездки, Приглашения, Уведомления, Профиль.
 *
 * Нижняя навигация остаётся видимой на всех этих экранах,
 * а selectedItem подсвечивает текущий раздел.
 */
@Composable
fun MainTabScaffold(
    title: String,
    selectedItem: TripsBottomItem,
    showCreateButton: Boolean = false,
    onCreateTripClick: () -> Unit = {},
    onTripsClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TripsTopBar(title = title)
        },
        floatingActionButton = {
            if (showCreateButton) {
                FloatingActionButton(
                    onClick = onCreateTripClick,
                    containerColor = AppBlue,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Создать поездку"
                    )
                }
            }
        },
        bottomBar = {
            TripsBottomBar(
                selectedItem = selectedItem,
                onTripsClick = onTripsClick,
                onInvitationsClick = onInvitationsClick,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        content = content
    )
}

/**
 * Scaffold конкретно для экрана "Мои поездки".
 */
@Composable
fun TripsHomeScaffold(
    onCreateTripClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    MainTabScaffold(
        title = "Мои поездки",
        selectedItem = TripsBottomItem.TRIPS,
        showCreateButton = true,
        onCreateTripClick = onCreateTripClick,
        onTripsClick = {},
        onInvitationsClick = onInvitationsClick,
        onNotificationsClick = onNotificationsClick,
        onProfileClick = onProfileClick,
        content = content
    )
}

/**
 * Верхняя панель.
 *
 * statusBarsPadding() защищает от наложения системной строки телефона.
 */
/**
 * Верхняя панель главных разделов.
 */
@Composable
private fun TripsTopBar(
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(70.dp)
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppDarkText
        )
    }
}

/**
 * Спокойная карточка поездки.
 *
 * Здесь нет ярко-синей заливки, потому что на экране и так много акцентов.
 * Количество участников и точек не показываем, пока эти данные не считаются реально.
 */
@Composable
fun TripHomeCard(
    trip: Trip,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accentColor = when (trip.status) {
        TripStatus.PLANNING -> Color(0xFF2563EB)
        TripStatus.ACTIVE -> Color(0xFF16A34A)
        TripStatus.COMPLETED -> Color(0xFF6B7280)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppDarkText,
                        modifier = Modifier.weight(1f)
                    )

                    TripStatusPill(status = trip.status)
                }

                if (trip.description.isNotBlank()) {
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppMutedText
                    )
                }

                Text(
                    text = "${trip.startDate} — ${trip.endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppDarkText
                )
            }
        }
    }
}

/**
 * Плашка статуса поездки.
 */
@Composable
private fun TripStatusPill(
    status: TripStatus
) {
    val text = when (status) {
        TripStatus.PLANNING -> "В планировании"
        TripStatus.ACTIVE -> "В процессе"
        TripStatus.COMPLETED -> "Завершена"
    }

    val background = when (status) {
        TripStatus.PLANNING -> Color(0xFFEFF6FF)
        TripStatus.ACTIVE -> Color(0xFFEAF7EE)
        TripStatus.COMPLETED -> Color(0xFFF3F4F6)
    }

    val content = when (status) {
        TripStatus.PLANNING -> Color(0xFF2563EB)
        TripStatus.ACTIVE -> Color(0xFF15803D)
        TripStatus.COMPLETED -> Color(0xFF6B7280)
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(50.dp)
    ) {
        Text(
            text = text,
            color = content,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Нижнее меню.
 */
@Composable
fun TripsBottomBar(
    selectedItem: TripsBottomItem,
    onTripsClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = selectedItem == TripsBottomItem.TRIPS,
            onClick = onTripsClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.CardTravel,
                    contentDescription = "Поездки"
                )
            },
            label = { Text("Поездки") },
            colors = bottomItemColors()
        )

        NavigationBarItem(
            selected = selectedItem == TripsBottomItem.INVITATIONS,
            onClick = onInvitationsClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.GroupAdd,
                    contentDescription = "Приглашения"
                )
            },
            label = { Text("Приглашения") },
            colors = bottomItemColors()
        )

        NavigationBarItem(
            selected = selectedItem == TripsBottomItem.NOTIFICATIONS,
            onClick = onNotificationsClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Уведомления"
                )
            },
            label = { Text("Уведомления") },
            colors = bottomItemColors()
        )

        NavigationBarItem(
            selected = selectedItem == TripsBottomItem.PROFILE,
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Профиль"
                )
            },
            label = { Text("Профиль") },
            colors = bottomItemColors()
        )
    }
}

@Composable
private fun bottomItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppBlue,
    selectedTextColor = AppBlue,
    indicatorColor = Color(0xFFEFF6FF),
    unselectedIconColor = AppMutedText,
    unselectedTextColor = AppMutedText
)


enum class TripsBottomItem {
    TRIPS,
    INVITATIONS,
    NOTIFICATIONS,
    PROFILE
}
