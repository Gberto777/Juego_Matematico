package com.mathquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mathquest.ui.login.LoginView

/**
 * Activity de arranque de MathQuest.
 *
 * Configuracion minima: unicamente monta el arbol de Compose y muestra
 * [LoginView] como pantalla inicial. No contiene logica de negocio ni de
 * red; eso vive en el ViewModel/Repository correspondientes.
 */
class MainActivity : ComponentActivity() {
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
