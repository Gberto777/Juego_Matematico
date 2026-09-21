package com.mathquest.repository

import com.mathquest.api.MathQuestApiService
import com.mathquest.api.RetrofitClient
import com.mathquest.model.LoginRequest
import com.mathquest.model.LoginResponse
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository de autenticacion (patron Repository de MVVM).
 *
 * Aisla a [com.mathquest.viewmodel.LoginViewModel] del detalle de
 * transporte HTTP: el ViewModel solo conoce [LoginRepository.login] y su
 * resultado tipado ([LoginResult]), nunca [MathQuestApiService] ni
 * [RetrofitClient] directamente. Esto facilita sustituir la
 * implementacion (por ejemplo por un fake/mock en tests unitarios del
 * ViewModel) sin tocar la capa de presentacion.
 *
 * Como Repository segun el patron clasico, coordina las DOS fuentes de
 * datos del login: la remota (HTTP via [apiService]) y la local
 * ([sessionManager], persistencia cifrada del token). Por eso, cuando
 * el backend confirma credenciales validas, este Repository persiste el
 * `token_jwt` el mismo antes de devolver [LoginResult.Success]; el
 * ViewModel no conoce [SessionManager] en absoluto, solo reacciona al
 * resultado para actualizar el estado de la UI.
 */
class LoginRepository(
    private val sessionManager: SessionManager,
    private val apiService: MathQuestApiService = RetrofitClient.apiService
) {

    /** Resultado del intento de login, ya interpretado (sin detalles de HTTP). */
    sealed class LoginResult {
        data class Success(val response: LoginResponse) : LoginResult()
        data class Failure(val message: String) : LoginResult()
    }

    /**
     * Envia email/password al backend real via Retrofit.
     * Nunca lanza excepciones: cualquier error de red o del servidor se
     * traduce a [LoginResult.Failure] con un mensaje apto para mostrar
     * en la UI. Las excepciones de red mas comunes (sin Internet, host
     * inalcanzable, timeout) se atrapan explicitamente para dar un
     * mensaje mas preciso que el catch generico de IOException.
     */
    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val httpResponse = apiService.login(LoginRequest(email = email, password = password))
            val body = httpResponse.body()

            if (httpResponse.isSuccessful && body != null) {
                // Credenciales validas: persistimos el token + id_usuario
                // de forma segura antes de reportar el exito al ViewModel.
                // El id_usuario se guarda para que DashboardViewModel pueda
                // pedir el progreso sin volver a decodificar el JWT.
                sessionManager.saveSession(idUsuario = body.idUsuario, tokenJwt = body.tokenJwt)
                LoginResult.Success(body)
            } else {
                LoginResult.Failure(
                    "Credenciales inválidas o error del servidor (código ${httpResponse.code()})."
                )
            }
        } catch (e: UnknownHostException) {
            // No se pudo resolver el host del backend: tipicamente el
            // dispositivo no tiene conexion de red en absoluto.
            LoginResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: ConnectException) {
            // No se pudo establecer la conexion TCP (servidor caido,
            // inalcanzable, o sin red).
            LoginResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: SocketTimeoutException) {
            // La conexion o la respuesta tardaron demasiado (red lenta
            // o inestable).
            LoginResult.Failure(MENSAJE_SIN_INTERNET)
        } catch (e: IOException) {
            // Cualquier otro error de I/O de red no cubierto arriba.
            LoginResult.Failure(MENSAJE_ERROR_GENERICO)
        } catch (e: Exception) {
            LoginResult.Failure(MENSAJE_ERROR_GENERICO)
        }
    }

    private companion object {
        private const val MENSAJE_SIN_INTERNET = "No hay conexión a Internet."
        private const val MENSAJE_ERROR_GENERICO = "Existe un error. Intenta nuevamente."
    }
}
