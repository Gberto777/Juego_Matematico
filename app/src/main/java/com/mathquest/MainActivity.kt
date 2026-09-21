package com.mathquest

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathquest.ui.dashboard.DashboardView
import com.mathquest.ui.login.LoginView
import com.mathquest.viewmodel.LoginUiState
import com.mathquest.viewmodel.LoginViewModel

/**
 * Activity de arranque de MathQuest.
 *
 * Hereda de [FragmentActivity] (en vez de ComponentActivity) porque
 * androidx.biometric.BiometricPrompt requiere un FragmentActivity como
 * host para poder mostrar el dialogo biometrico nativo del sistema desde
 * [LoginView]. setContent sigue funcionando igual, ya que FragmentActivity
 * extiende de ComponentActivity.
 *
 * Navegacion: se usa un simple condicional de Compose (en vez de
 * Jetpack Navigation) basado en [LoginUiState]. Es la opcion mas simple
 * para un flujo de solo dos pantallas; si el numero de pantallas crece,
 * migrar a androidx.navigation:navigation-compose con un NavHost.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val loginViewModel: LoginViewModel = viewModel()
                    val uiState by loginViewModel.uiState.collectAsState()

                    if (uiState is LoginUiState.Success) {
                        DashboardView()
                    } else {
                        LoginView(viewModel = loginViewModel)
                    }
                }
            }
        }
    }
}
