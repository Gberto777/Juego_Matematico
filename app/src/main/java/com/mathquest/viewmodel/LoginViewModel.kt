package com.mathquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathquest.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
 * El login por email/password ahora delega en [LoginRepository] (patron
 * Repository), que a su vez habla con el backend real via Retrofit
 * ([com.mathquest.api.RetrofitClient]). El ViewModel nunca conoce
 * Retrofit ni la URL base directamente, solo el contrato del Repository.
 *
 * @JvmOverloads es necesario porque la factory por defecto de
 * `viewModel()` (Compose) instancia el ViewModel via reflection buscando
 * un constructor sin argumentos; sin esta anotacion, Kotlin solo emite
 * en bytecode el constructor con el parametro (aunque tenga valor por
 * defecto), y esa instanciacion fallaria en tiempo de ejecucion.
 */
class LoginViewModel @JvmOverloads constructor(
    private val loginRepository: LoginRepository = LoginRepository()
) : ViewModel() {

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
     * Envia el email/password actuales al backend real a traves de
     * [LoginRepository] (Retrofit) y mapea el resultado al estado de la UI.
     */
    fun onLoginClick() {
        val currentEmail = _email.value
        val currentPassword = _password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _uiState.value = LoginUiState.Error("El correo y la contraseña son obligatorios")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            when (val result = loginRepository.login(currentEmail, currentPassword)) {
                is LoginRepository.LoginResult.Success -> {
                    _uiState.value = LoginUiState.Success
                }
                is LoginRepository.LoginResult.Failure -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
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
