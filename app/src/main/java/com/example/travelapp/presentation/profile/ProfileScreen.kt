package com.example.travelapp.presentation.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ProfileScreen — экран профиля пользователя.
 *
 * Здесь отображаются данные текущего пользователя
 * и доступна кнопка выхода из аккаунта.
 */
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onSignedOut: () -> Unit
) {
    /**
     * Если пользователь вышел из аккаунта,
     * переходим обратно на экран входа.
     */
    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onSignedOut()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Профиль пользователя")
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(text = "Имя: ${uiState.name}")
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Email: ${uiState.email}")
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "ID пользователя: ${uiState.userId}")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Выйти из аккаунта")
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}