package com.mathquest.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Pantalla de destino tras un login exitoso (por email/password o por
 * desbloqueo biometrico).
 *
 * Por ahora es un placeholder minimo que solo confirma visualmente que
 * la sesion se inicio correctamente. El contenido real del dashboard
 * (progreso, niveles, etc., consumiendo GET /api/progreso/{id}) se
 * construira en una iteracion posterior.
 */
@Composable
fun DashboardView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "¡Sesión iniciada correctamente!\nBienvenido a MathQuest.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardViewPreview() {
    MaterialTheme {
        DashboardView()
    }
}
