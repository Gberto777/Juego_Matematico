package com.mathquest.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
 * Muestra el historial de progreso del usuario en una [LazyColumn].
 * Un boton flotante abre un dialogo modal para registrar un avance
 * nuevo (POST); cada item de la lista tiene iconos de editar (PUT) y
 * eliminar (DELETE, con dialogo de confirmacion). Todas las acciones
 * exitosas refrescan la lista mostrada con la respuesta real del
 * backend.
 *
 * Ademas del texto de error inline (persistente, arriba de la lista),
 * cada nuevo [DashboardUiState.errorMessage] no nulo dispara un
 * Snackbar transitorio (ej. errores de red como "No hay conexión a
 * Internet.", generados en [com.mathquest.repository.ProgresoRepository]).
 */
@Composable
fun DashboardView(
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onCreateProgreso = viewModel::registrarProgreso,
        onUpdateProgreso = viewModel::actualizarProgreso,
        onDeleteProgreso = viewModel::eliminarProgreso
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onCreateProgreso: (nivel: Int, puntaje: Int) -> Unit,
    onUpdateProgreso: (idRegistro: String, nivel: Int, puntaje: Int) -> Unit,
    onDeleteProgreso: (idRegistro: String) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    // ProgresoResponse no es Parcelable, por eso `remember` (no
    // `rememberSaveable`): perder un dialogo abierto ante un cambio de
    // configuracion/proceso es un costo aceptable para este alcance.
    var editingProgreso by remember { mutableStateOf<ProgresoResponse?>(null) }
    var deletingProgreso by remember { mutableStateOf<ProgresoResponse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Cada operacion (cargar/crear/editar/eliminar) limpia errorMessage
    // a null antes de empezar, asi que dos fallos consecutivos con el
    // MISMO texto igual disparan el Snackbar dos veces (la key pasa por
    // null en medio).
    LaunchedEffect(uiState.errorMessage) {
        val mensaje = uiState.errorMessage
        if (mensaje != null) {
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
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
                Spacer(modifier = Modifier.height(8.dp))

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    uiState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.progresos.isEmpty() -> {
                        Text(
                            text = "Aún no tienes progreso registrado. Usa el botón + para agregar uno.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(uiState.progresos) { progreso ->
                                ProgresoItem(
                                    progreso = progreso,
                                    onEditClick = { editingProgreso = progreso },
                                    onDeleteClick = { deletingProgreso = progreso }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ProgresoFormDialog(
            title = "Registrar progreso",
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showAddDialog = false },
            onConfirm = { nivel, puntaje ->
                onCreateProgreso(nivel, puntaje)
                showAddDialog = false
            }
        )
    }

    editingProgreso?.let { progreso ->
        ProgresoFormDialog(
            title = "Editar progreso",
            initialNivel = progreso.nivelAlcanzado,
            initialPuntaje = progreso.puntaje,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { editingProgreso = null },
            onConfirm = { nivel, puntaje ->
                onUpdateProgreso(progreso.idRegistro, nivel, puntaje)
                editingProgreso = null
            }
        )
    }

    deletingProgreso?.let { progreso ->
        AlertDialog(
            onDismissRequest = { deletingProgreso = null },
            title = { Text("Eliminar progreso") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar el registro de Nivel " +
                        "${progreso.nivelAlcanzado} · ${progreso.puntaje} pts? " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProgreso(progreso.idRegistro)
                    deletingProgreso = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProgreso = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProgresoItem(
    progreso: ProgresoResponse,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nivel ${progreso.nivelAlcanzado} · ${progreso.puntaje} pts",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = progreso.fechaActualizacion,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar progreso")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar progreso")
            }
        }
    }
}

/**
 * Dialogo modal reutilizado tanto para registrar (campos vacios) como
 * para editar (campos pre-cargados con [initialNivel]/[initialPuntaje])
 * un avance. Solo pide Nivel y Puntaje: el id_usuario/id_registro lo
 * resuelve el llamador, nunca se piden aqui.
 */
@Composable
private fun ProgresoFormDialog(
    title: String,
    initialNivel: Int? = null,
    initialPuntaje: Int? = null,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nivel: Int, puntaje: Int) -> Unit
) {
    var nivelText by rememberSaveable { mutableStateOf(initialNivel?.toString() ?: "") }
    var puntajeText by rememberSaveable { mutableStateOf(initialPuntaje?.toString() ?: "") }

    val nivel = nivelText.toIntOrNull()
    val puntaje = puntajeText.toIntOrNull()
    val esValido = nivel != null && nivel >= 1 && puntaje != null && puntaje >= 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
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
                        nivelAlcanzado = 7,
                        puntaje = 1500,
                        fechaActualizacion = "2026-09-21T14:41:57.000Z"
                    ),
                    ProgresoResponse(
                        idRegistro = "preview-2",
                        idUsuario = "preview-user",
                        nivelAlcanzado = 3,
                        puntaje = 450,
                        fechaActualizacion = "2026-09-10T10:15:00.000Z"
                    )
                )
            ),
            onCreateProgreso = { _, _ -> },
            onUpdateProgreso = { _, _, _ -> },
            onDeleteProgreso = { }
        )
    }
}
