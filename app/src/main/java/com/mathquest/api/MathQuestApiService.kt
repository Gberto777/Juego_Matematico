package com.mathquest.api

import com.mathquest.model.LoginRequest
import com.mathquest.model.LoginResponse
import com.mathquest.model.ProgresoRequest
import com.mathquest.model.ProgresoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Contrato Retrofit de la API REST de MathQuest.
 *
 * Todos los metodos son `suspend`: Retrofit ejecuta la llamada en un
 * hilo de I/O y suspende la corrutina que lo invoca (tipicamente desde
 * viewModelScope en el Repository/ViewModel), sin bloquear el hilo
 * principal.
 *
 * [getProgreso] y [crearProgreso] requieren un usuario autenticado: el
 * header `Authorization: Bearer <token>` se inyecta automaticamente via
 * el interceptor configurado en [RetrofitClient], no aqui.
 */
interface MathQuestApiService {

    /**
     * Autentica un usuario por email/password.
     * Devuelve el token JWT + datos basicos del usuario si es exitoso.
     */
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * Obtiene el progreso (nivel/puntaje) mas reciente de un usuario.
     */
    @GET("/api/progreso/{id}")
    suspend fun getProgreso(@Path("id") idUsuario: String): Response<ProgresoResponse>

    /**
     * Registra un nuevo avance (nivel alcanzado + puntaje) para el
     * usuario autenticado (el backend deriva el id_usuario del JWT).
     */
    @POST("/api/progreso")
    suspend fun crearProgreso(@Body request: ProgresoRequest): Response<ProgresoResponse>
}
