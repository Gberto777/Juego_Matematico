package com.mathquest

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.mathquest.ui.login.LoginView

/**
 * Activity de arranque de MathQuest.
 *
 * Hereda de [FragmentActivity] (en vez de ComponentActivity) porque
 * androidx.biometric.BiometricPrompt requiere un FragmentActivity como
 * host para poder mostrar el dialogo biometrico nativo del sistema desde
 * [LoginView]. setContent sigue funcionando igual, ya que FragmentActivity
 * extiende de ComponentActivity.
 *
 * Configuracion minima: unicamente monta el arbol de Compose y muestra
 * [LoginView] como pantalla inicial. No contiene logica de negocio ni de
 * red; eso vive en el ViewModel/Repository correspondientes.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginView()
                }
            }
        }
    }
}
