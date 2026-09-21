package com.mathquest.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un unico registro de la tabla `progreso`
 * (docs/mathquest_init_schema.sql).
 *
 * Se usa como:
 *   - Elemento de la lista devuelta por GET /api/progreso/{id}
 *     (`List<ProgresoResponse>`, el historial completo del usuario).
 *   - Cuerpo de respuesta de POST /api/progreso y PUT /api/progreso/{id}
 *     (un solo registro creado/actualizado).
 */
data class ProgresoResponse(
    @SerializedName("id_registro")
    val idRegistro: String,
    @SerializedName("id_usuario")
    val idUsuario: String,
    @SerializedName("nivel_alcanzado")
    val nivelAlcanzado: Int,
    @SerializedName("puntaje")
    val puntaje: Int,
    @SerializedName("fecha_actualizacion")
    val fechaActualizacion: String
)
