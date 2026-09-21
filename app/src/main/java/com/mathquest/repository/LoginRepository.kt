package com.mathquest.repository

import com.mathquest.api.MathQuestApiService
import com.mathquest.api.RetrofitClient
import com.mathquest.model.LoginRequest
import com.mathquest.model.LoginResponse
import java.io.IOException

/**
 * Repository de autenticacion (patron Repository de MVVM).
 *
 * Aisla a [com.mathquest.viewmodel.LoginViewModel] del detalle de
 * transporte HTTP: el ViewModel solo conoce [LoginRepository.login] y su
 * resultado tipado ([LoginResult]), nunca [MathQuestApiService] ni
 * [RetrofitClient] directamente. Esto facilita sustituir la
 * implementacion (por ejemplo por un fake/mock en tests unitarios del
 * ViewModel) sin tocar la capa de presentacion.
 */
class LoginRepository(
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
     * en la UI.
     */
    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val httpResponse = apiService.login(LoginRequest(email = email, password = password))
            val body = httpResponse.body()

            if (httpResponse.isSuccessful && body != null) {
                LoginResult.Success(body)
            } else {
                LoginResult.Failure(
                    "Credenciales inválidas o error del servidor (código ${httpResponse.code()})."
                )
            }
        } catch (e: IOException) {
            LoginResult.Failure("No se pudo conectar con el servidor. Verifica tu conexión.")
        } catch (e: Exception) {
            LoginResult.Failure("Ocurrió un error inesperado al iniciar sesión.")
        }
    }
}
