package com.mathquest.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathquest.viewmodel.LoginUiState
import com.mathquest.viewmodel.LoginViewModel

/**
 * Pantalla de login de MathQuest (Jetpack Compose).
 *
 * Se conecta con [LoginViewModel] via StateFlow (email, password, uiState).
 * El login por email/password todavia es simulado (sin Retrofit), pero el
 * boton de "Desbloqueo Biométrico" ya dispara el BiometricPrompt real del
 * sistema operativo a traves de [BiometricAuthenticator]; el resultado
 * (exito/fallo) se reporta al [LoginViewModel] para actualizar el estado.
 */
@Composable
fun LoginView(
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()

    // El host de BiometricPrompt debe ser un FragmentActivity; MainActivity
    // ya hereda de FragmentActivity, por lo que el context de Compose
    // siempre puede castearse aqui de forma segura.
    val context = LocalContext.current
    val hostActivity = context as? FragmentActivity

    val onBiometricUnlockClick: () -> Unit = {
        viewModel.onBiometricUnlockRequested()
        if (hostActivity != null) {
            BiometricAuthenticator.authenticate(
                activity = hostActivity,
                onSuccess = viewModel::onBiometricAuthSucceeded,
                onFailure = viewModel::onBiometricAuthFailed
            )
        } else {
            viewModel.onBiometricAuthFailed(
                "No se pudo iniciar el desbloqueo biométrico. Usa tu contraseña."
            )
        }
    }

    LoginContent(
        email = email,
        password = password,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onBiometricUnlockClick = onBiometricUnlockClick
    )
}

@Composable
private fun LoginContent(
    email: String,
    password: String,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onBiometricUnlockClick: () -> Unit
) {
    val isLoading = uiState is LoginUiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "MathQuest",
                style = MaterialTheme.typography.headlineMedium
            )

            // Campo de correo electronico.
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de contraseña oculto (visualTransformation enmascara el texto).
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState is LoginUiState.Error) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Boton principal de inicio de sesion.
            Button(
                onClick = onLoginClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar sesión")
                }
            }

            // Boton secundario, claramente diferenciado (estilo "outlined"),
            // para el desbloqueo biometrico.
            OutlinedButton(
                onClick = onBiometricUnlockClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Desbloqueo Biométrico")
            }

            if (uiState is LoginUiState.Success) {
                Text(
                    text = "Sesión iniciada correctamente",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentIdlePreview() {
    MaterialTheme {
        LoginContent(
            email = "",
            password = "",
            uiState = LoginUiState.Idle,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onBiometricUnlockClick = {}
        )
    }
}
