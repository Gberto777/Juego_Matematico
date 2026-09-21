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

    /** Resultado de una operacion de progreso, ya interpretado (sin detalles de HTTP). */
    sealed class ProgresoResult {
        data class Success(val progreso: ProgresoResponse) : ProgresoResult()
        data class Failure(val message: String) : ProgresoResult()
    }

    /** Obtiene el registro de progreso mas reciente del usuario indicado. */
    suspend fun obtenerProgreso(idUsuario: String): ProgresoResult {
        return try {
            val httpResponse = apiService.getProgreso(idUsuario)
            val body = httpResponse.body()

            when {
                httpResponse.isSuccessful && body != null -> ProgresoResult.Success(body)
                httpResponse.code() == 404 -> ProgresoResult.Failure(
                    "Aún no tienes progreso registrado. ¡Usa el botón + para agregar el primero!"
                )
                else -> ProgresoResult.Failure(
                    "No se pudo obtener el progreso (código ${httpResponse.code()})."
                )
            }
        } catch (e: IOException) {
            ProgresoResult.Failure("No se pudo conectar con el servidor. Verifica tu conexión.")
        } catch (e: Exception) {
            ProgresoResult.Failure("Ocurrió un error inesperado al obtener el progreso.")
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
}
