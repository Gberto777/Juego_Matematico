package com.mathquest.repository

import com.mathquest.api.MathQuestApiService
import com.mathquest.api.RetrofitClient
import com.mathquest.model.ProgresoRequest
import com.mathquest.model.ProgresoResponse
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository de progreso (patron Repository de MVVM).
 *
 * Aisla a [com.mathquest.viewmodel.DashboardViewModel] del detalle de
 * transporte HTTP. No maneja el token de autenticacion directamente:
 * el interceptor configurado en [RetrofitClient] lo inyecta
 * automaticamente en cada peticion a partir de [SessionManager].
 *
 * Las excepciones de red mas comunes (sin Internet, host inalcanzable,
 * timeout) se atrapan explicitamente en cada metodo para dar un
 * mensaje mas preciso ("No hay conexión a Internet.") que el catch
 * generico de errores inesperados ("Existe un error.").
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
        } catch (e: UnknownHostException) {
            ProgresoListResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: ConnectException) {
            ProgresoListResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: SocketTimeoutException) {
            ProgresoListResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: IOException) {
            ProgresoListResult.Failure(MENSAJE_ERROR_GENERICO)
        } catch (e: Exception) {
            ProgresoListResult.Failure(MENSAJE_ERROR_GENERICO)
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
        } catch (e: UnknownHostException) {
            ProgresoResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: ConnectException) {
            ProgresoResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: SocketTimeoutException) {
            ProgresoResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: IOException) {
            ProgresoResult.Failure(MENSAJE_ERROR_GENERICO)
        } catch (e: Exception) {
            ProgresoResult.Failure(MENSAJE_ERROR_GENERICO)
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
        } catch (e: UnknownHostException) {
            ProgresoResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: ConnectException) {
            ProgresoResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: SocketTimeoutException) {
            ProgresoResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: IOException) {
            ProgresoResult.Failure(MENSAJE_ERROR_GENERICO)
        } catch (e: Exception) {
            ProgresoResult.Failure(MENSAJE_ERROR_GENERICO)
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
        } catch (e: UnknownHostException) {
            AccionResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: ConnectException) {
            AccionResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: SocketTimeoutException) {
            AccionResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: IOException) {
            AccionResult.Failure(MENSAJE_ERROR_GENERICO)
        } catch (e: Exception) {
            AccionResult.Failure(MENSAJE_ERROR_GENERICO)
        }
    }

    private companion object {
        private const val MENSAJE_SIN_INTERNET = "No hay conexión a Internet."
        private const val MENSAJE_ERROR_GENERICO = "Existe un error. Intenta nuevamente."
    }
}
