package com.mathquest.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathquest.model.ProgresoResponse
import com.mathquest.viewmodel.DashboardUiState
import com.mathquest.viewmodel.DashboardViewModel

/**
 * Pantalla de destino tras un login exitoso (por email/password o por
 * desbloqueo biometrico).
 *
 * Muestra el progreso del usuario en una [LazyColumn] y permite
 * registrar un nuevo avance (nivel/puntaje) mediante un boton flotante
 * que abre un dialogo modal; al confirmar, el registro se envia a la
 * API (POST /api/progreso) y, si tiene exito, se agrega a la lista.
 */
@Composable
fun DashboardView(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onSubmitProgreso = viewModel::registrarProgreso
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onSubmitProgreso: (nivel: Int, puntaje: Int) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar progreso")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Mi progreso en MathQuest",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.progresos.isEmpty() -> {
                        Text(
                            text = uiState.errorMessage
                                ?: "Aún no tienes progreso registrado. Usa el botón + para agregar uno.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(uiState.progresos) { progreso ->
                                ProgresoItem(progreso)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        NuevoProgresoDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { nivel, puntaje ->
                onSubmitProgreso(nivel, puntaje)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ProgresoItem(progreso: ProgresoResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nivel ${progreso.nivelAlcanzado} · ${progreso.puntaje} pts",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = progreso.fechaActualizacion,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Dialogo modal para registrar un nuevo avance. Solo pide Nivel y
 * Puntaje: el id_usuario lo resuelve el backend a partir del JWT, nunca
 * se envia desde aqui (ver ProgresoRequest.kt).
 */
@Composable
private fun NuevoProgresoDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nivel: Int, puntaje: Int) -> Unit
) {
    var nivelText by rememberSaveable { mutableStateOf("") }
    var puntajeText by rememberSaveable { mutableStateOf("") }

    val nivel = nivelText.toIntOrNull()
    val puntaje = puntajeText.toIntOrNull()
    val esValido = nivel != null && nivel >= 1 && puntaje != null && puntaje >= 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Registrar progreso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nivelText,
                    onValueChange = { nuevoValor -> nivelText = nuevoValor.filter { it.isDigit() } },
                    label = { Text("Nivel alcanzado") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = puntajeText,
                    onValueChange = { nuevoValor -> puntajeText = nuevoValor.filter { it.isDigit() } },
                    label = { Text("Puntaje") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nivelValido = nivel
                    val puntajeValido = puntaje
                    if (nivelValido != null && puntajeValido != null) {
                        onConfirm(nivelValido, puntajeValido)
                    }
                },
                enabled = esValido && !isSubmitting
            ) {
                Text(if (isSubmitting) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    MaterialTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                progresos = listOf(
                    ProgresoResponse(
                        idRegistro = "preview-1",
                        idUsuario = "preview-user",
                        nivelAlcanzado = 5,
                        puntaje = 980,
                        fechaActualizacion = "2026-09-18T16:40:00.000Z"
                    )
                )
            ),
            onSubmitProgreso = { _, _ -> }
        )
    }
}
