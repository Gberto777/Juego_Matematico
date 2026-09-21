package com.mathquest.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estados posibles de la pantalla de login.
 * Se modela como sealed class para que el StateFlow emita un tipo cerrado
 * y la UI pueda reaccionar de forma exhaustiva (when) a cada caso.
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object Success : LoginUiState()
}

/**
 * ViewModel de la pantalla de login (patron MVVM).
 *
 * Expone el estado de la UI (idle/loading/error/success) y los valores de
 * los campos de entrada como StateFlow, para que LoginView los consuma de
 * forma reactiva mediante collectAsState().
 *
 * NOTA: Todavia no depende de un Repository/API real (Retrofit). La
 * validacion y el "login" actuales son simulados unicamente para conectar
 * el flujo de estados con la UI; se reemplazaran cuando se incorpore la
 * capa de red.
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        clearErrorIfPresent()
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        clearErrorIfPresent()
    }

    /**
     * Simula el intento de inicio de sesion con email/password.
     * TODO: sustituir por la llamada real a un LoginRepository (Retrofit)
     * cuando la capa de red este implementada.
     */
    fun onLoginClick() {
        val currentEmail = _email.value
        val currentPassword = _password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _uiState.value = LoginUiState.Error("El correo y la contraseña son obligatorios")
            return
        }

        _uiState.value = LoginUiState.Loading

        // Placeholder sin logica de red: se resuelve como exito inmediato
        // solo para validar el cableado de estados con la vista.
        _uiState.value = LoginUiState.Success
    }

    /**
     * Se invoca justo antes de mostrar el BiometricPrompt del sistema
     * (disparado desde LoginView al pulsar "Desbloqueo Biométrico").
     */
    fun onBiometricUnlockRequested() {
        _uiState.value = LoginUiState.Loading
    }

    /**
     * El sistema biometrico seguro del OS valido la huella/rostro como
     * autentica (BiometricPrompt.AuthenticationCallback#onAuthenticationSucceeded).
     * La app nunca ve ni almacena la huella en si, solo este resultado.
     */
    fun onBiometricAuthSucceeded() {
        _uiState.value = LoginUiState.Success
    }

    /**
     * La huella/rostro fue invalida, no hay biometria enrolada, el
     * hardware no esta disponible, o el usuario cancelo el prompt. En
     * todos los casos se deniega el acceso mediante biometria y se
     * obliga al usuario a autenticarse con el input de contraseña
     * (que permanece visible y habilitado en LoginView).
     */
    fun onBiometricAuthFailed(reason: String) {
        _uiState.value = LoginUiState.Error(reason)
    }

    private fun clearErrorIfPresent() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }
}
