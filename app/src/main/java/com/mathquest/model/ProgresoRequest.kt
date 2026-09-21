package com.mathquest.model

import com.google.gson.annotations.SerializedName

/**
 * Cuerpo (body) de la peticion POST /api/progreso.
 *
 * Deliberadamente NO incluye `id_usuario`: el backend lo obtiene del
 * usuario autenticado en el JWT (`Authorization: Bearer <token>`,
 * inyectado por el interceptor de [com.mathquest.api.RetrofitClient]),
 * para que un usuario nunca pueda registrar progreso a nombre de otro.
 */
data class ProgresoRequest(
    @SerializedName("nivel_alcanzado")
    val nivelAlcanzado: Int,
    @SerializedName("puntaje")
    val puntaje: Int
)
