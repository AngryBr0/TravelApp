package com.example.travelapp.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.travelapp.ui.components.AuthBackground
import com.example.travelapp.ui.components.AuthErrorText
import com.example.travelapp.ui.components.AuthHeader
import com.example.travelapp.ui.components.AuthPrimaryButton
import com.example.travelapp.ui.components.AuthSecondaryButton
import com.example.travelapp.ui.components.AuthTextField
import com.example.travelapp.ui.theme.TravelAppTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.VisualTransformation

/**
 * LoginScreen — экран входа.
 *
 * Здесь нет Google-кнопки и нет "Забыли пароль?",
 * потому что эта функциональность пока не реализована.
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

    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthorized) {
        if (uiState.isAuthorized) {
            onLoginSuccess()
        }
    }

    AuthBackground(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AuthHeader(
            title = "Добро пожаловать"
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Войдите, чтобы продолжить",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(28.dp))

        AuthTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "Введите email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(18.dp))

        AuthTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Пароль",
            placeholder = "Введите пароль",
            keyboardType = KeyboardType.Password,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        isPasswordVisible = !isPasswordVisible
                    }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (isPasswordVisible) {
                            "Скрыть пароль"
                        } else {
                            "Показать пароль"
                        }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        AuthErrorText(message = uiState.errorMessage)

        Spacer(modifier = Modifier.height(28.dp))

        AuthPrimaryButton(
            text = "Войти",
            onClick = onLoginClick,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(14.dp))

        AuthSecondaryButton(
            text = "Нет аккаунта? Зарегистрироваться",
            onClick = onRegisterClick
        )

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(20.dp))

            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    TravelAppTheme {
        LoginScreen(
            uiState = AuthUiState(
                email = "denis@mail.ru",
                password = "123456"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onLoginSuccess = {}
        )
    }
}