package com.mathquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mathquest.model.ProgresoResponse
import com.mathquest.repository.ProgresoRepository
import com.mathquest.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de dashboard/progreso.
 *
 * A diferencia de [LoginUiState] (un flujo lineal de pasos mutuamente
 * excluyentes), esta pantalla combina dos preocupaciones concurrentes
 * -la carga inicial de la lista y el envio del formulario del dialogo-,
 * por lo que se modela como un unico data class con banderas
 * independientes en vez de una sealed class de estados excluyentes.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val progresos: List<ProgresoResponse> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel del dashboard (patron MVVM).
 *
 * Al inicializarse ([init]), dispara automaticamente la consulta GET
 * del progreso del usuario autenticado via [ProgresoRepository]
 * (Retrofit, con el header Authorization inyectado por el interceptor
 * de [com.mathquest.api.RetrofitClient]). El id del usuario se lee de
 * [SessionManager], guardado ahi mismo tras un login exitoso
 * ([com.mathquest.repository.LoginRepository]).
 *
 * La API solo expone el registro MAS RECIENTE por usuario (no un
 * listado completo), asi que la "lista" que muestra la UI se construye
 * en el cliente: arranca con ese unico registro (si existe) y crece en
 * memoria con cada POST exitoso durante la sesion actual.
 */
class DashboardViewModel @JvmOverloads constructor(
    application: Application,
    private val progresoRepository: ProgresoRepository = ProgresoRepository(),
    private val sessionManager: SessionManager = SessionManager(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        cargarProgreso()
    }

    private fun cargarProgreso() {
        val idUsuario = sessionManager.getUserId()
        if (idUsuario.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No hay una sesión activa."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = progresoRepository.obtenerProgreso(idUsuario)) {
                is ProgresoRepository.ProgresoResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progresos = listOf(result.progreso),
                        errorMessage = null
                    )
                }
                is ProgresoRepository.ProgresoResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progresos = emptyList(),
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Envia un nuevo registro de progreso (Nivel/Puntaje) desde el
     * dialogo modal. Si tiene exito, el registro devuelto por el
     * backend (con su id_registro y fecha_actualizacion reales) se
     * agrega al inicio de la lista mostrada.
     */
    fun registrarProgreso(nivelAlcanzado: Int, puntaje: Int) {
        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = progresoRepository.registrarProgreso(nivelAlcanzado, puntaje)) {
                is ProgresoRepository.ProgresoResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        progresos = listOf(result.progreso) + _uiState.value.progresos
                    )
                }
                is ProgresoRepository.ProgresoResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /** Descarta el mensaje de error actual (ej. tras mostrarlo en un Snackbar/Toast). */
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
