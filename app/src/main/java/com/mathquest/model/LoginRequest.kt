package com.mathquest.model

import com.google.gson.annotations.SerializedName

/**
 * Cuerpo (body) de la peticion POST /api/auth/login.
 *
 * El password viaja en texto plano dentro del body HTTPS; nunca se
 * guarda ni se procesa en el cliente. El backend es responsable de
 * compararlo contra `password_hash` (ver docs/mathquest_init_schema.sql,
 * tabla `usuarios`).
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
