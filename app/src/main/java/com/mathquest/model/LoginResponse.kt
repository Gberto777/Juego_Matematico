package com.mathquest.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del backend a POST /api/auth/login.
 *
 * Los nombres de campo siguen la convencion snake_case usada en la base
 * de datos (ver docs/mathquest_init_schema.sql, tablas `usuarios` y
 * `sesiones`); @SerializedName mapea ese JSON a propiedades camelCase
 * idiomaticas en Kotlin.
 */
data class LoginResponse(
    @SerializedName("id_usuario")
    val idUsuario: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("rol")
    val rol: String,
    @SerializedName("token_jwt")
    val tokenJwt: String
)
