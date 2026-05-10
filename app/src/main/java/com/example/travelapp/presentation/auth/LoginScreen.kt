package com.example.travelapp.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Экран входа.
 *
 * Он только отображает интерфейс и вызывает функции,
 * которые приходят из ViewModel через параметры.
 */
@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    /**
     * Если ViewModel изменила isAuthorized на true,
     * значит вход успешен, можно перейти на экран поездок.
     */
    LaunchedEffect(uiState.isAuthorized) {
        if (uiState.isAuthorized) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Вход в приложение")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зарегистрироваться")
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}