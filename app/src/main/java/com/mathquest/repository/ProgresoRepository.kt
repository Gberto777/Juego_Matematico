package com.mathquest.repository

import com.mathquest.api.MathQuestApiService
import com.mathquest.api.RetrofitClient
import com.mathquest.model.ProgresoRequest
import com.mathquest.model.ProgresoResponse
import java.io.IOException

/**
 * Repository de progreso (patron Repository de MVVM).
 *
 * Aisla a [com.mathquest.viewmodel.DashboardViewModel] del detalle de
 * transporte HTTP. No maneja el token de autenticacion directamente:
 * el interceptor configurado en [RetrofitClient] lo inyecta
 * automaticamente en cada peticion a partir de [SessionManager].
 */
class ProgresoRepository(
    private val apiService: MathQuestApiService = RetrofitClient.apiService
) {

    /** Resultado de una operacion sobre un unico registro (crear/actualizar). */
    sealed class ProgresoResult {
        data class Success(val progreso: ProgresoResponse) : ProgresoResult()
        data class Failure(val message: String) : ProgresoResult()
    }

    /** Resultado de obtener el historial completo (lista) de un usuario. */
    sealed class ProgresoListResult {
        data class Success(val progresos: List<ProgresoResponse>) : ProgresoListResult()
        data class Failure(val message: String) : ProgresoListResult()
    }

    /** Resultado de una operacion sin cuerpo de respuesta (eliminar). */
    sealed class AccionResult {
        object Success : AccionResult()
        data class Failure(val message: String) : AccionResult()
    }

    /** Obtiene TODO el historial de progreso del usuario indicado (mas reciente primero). */
    suspend fun obtenerProgreso(idUsuario: String): ProgresoListResult {
        return try {
            val httpResponse = apiService.getProgreso(idUsuario)
            val body = httpResponse.body()

            if (httpResponse.isSuccessful && body != null) {
                ProgresoListResult.Success(body)
            } else {
                ProgresoListResult.Failure(
                    "No se pudo obtener el progreso (código ${httpResponse.code()})."
                )
            }
        } catch (e: IOException) {
            ProgresoListResult.Failure("No se pudo conectar con el servidor. Verifica tu conexión.")
        } catch (e: Exception) {
            ProgresoListResult.Failure("Ocurrió un error inesperado al obtener el progreso.")
        }
    }

    /**
     * Registra un nuevo avance (nivel/puntaje) para el usuario
     * autenticado. El backend obtiene el id_usuario del JWT, no de este
     * request (ver [ProgresoRequest]).
     */
    suspend fun registrarProgreso(nivelAlcanzado: Int, puntaje: Int): ProgresoResult {
        return try {
            val httpResponse = apiService.crearProgreso(
                ProgresoRequest(nivelAlcanzado = nivelAlcanzado, puntaje = puntaje)
            )
            val body = httpResponse.body()

            if (httpResponse.isSuccessful && body != null) {
                ProgresoResult.Success(body)
            } else {
                ProgresoResult.Failure(
                    "No se pudo registrar el progreso (código ${httpResponse.code()})."
                )
            }
        } catch (e: IOException) {
            ProgresoResult.Failure("No se pudo conectar con el servidor. Verifica tu conexión.")
        } catch (e: Exception) {
            ProgresoResult.Failure("Ocurrió un error inesperado al registrar el progreso.")
        }
    }

    /**
     * Actualiza nivel/puntaje de un registro existente. El backend
     * responde 404 si el registro no existe o no pertenece al usuario
     * del token; ese caso se traduce aqui a un mensaje generico (no se
     * distingue "no existe" de "no es tuyo", igual que el backend).
     */
    suspend fun actualizarProgreso(
        idRegistro: String,
        nivelAlcanzado: Int,
        puntaje: Int
    ): ProgresoResult {
        return try {
            val httpResponse = apiService.actualizarProgreso(
                idRegistro,
                ProgresoRequest(nivelAlcanzado = nivelAlcanzado, puntaje = puntaje)
            )
            val body = httpResponse.body()

            if (httpResponse.isSuccessful && body != null) {
                ProgresoResult.Success(body)
            } else if (httpResponse.code() == 404) {
                ProgresoResult.Failure("Ese registro ya no existe o no te pertenece.")
            } else {
                ProgresoResult.Failure(
                    "No se pudo actualizar el progreso (código ${httpResponse.code()})."
                )
            }
        } catch (e: IOException) {
            ProgresoResult.Failure("No se pudo conectar con el servidor. Verifica tu conexión.")
        } catch (e: Exception) {
            ProgresoResult.Failure("Ocurrió un error inesperado al actualizar el progreso.")
        }
    }

    /**
     * Elimina un registro existente. Mismo criterio de propiedad que
     * [actualizarProgreso]: 404 si no existe o no es del usuario.
     */
    suspend fun eliminarProgreso(idRegistro: String): AccionResult {
        return try {
            val httpResponse = apiService.eliminarProgreso(idRegistro)

            if (httpResponse.isSuccessful) {
                AccionResult.Success
            } else if (httpResponse.code() == 404) {
                AccionResult.Failure("Ese registro ya no existe o no te pertenece.")
            } else {
                AccionResult.Failure(
                    "No se pudo eliminar el progreso (código ${httpResponse.code()})."
                )
            }
        } catch (e: IOException) {
            AccionResult.Failure("No se pudo conectar con el servidor. Verifica tu conexión.")
        } catch (e: Exception) {
            AccionResult.Failure("Ocurrió un error inesperado al eliminar el progreso.")
        }
    }
}
