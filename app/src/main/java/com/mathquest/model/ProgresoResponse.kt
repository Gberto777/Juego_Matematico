package com.mathquest.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del backend a GET /api/progreso/{id}.
 *
 * Espejo de la tabla `progreso` (ver docs/mathquest_init_schema.sql).
 * Se agrega junto con el resto de la capa de red (Sprint 4) para que
 * [com.mathquest.api.MathQuestApiService.getProgreso] tenga un tipo de
 * retorno tipado; el consumo real desde un ViewModel/Repository de
 * progreso se conectara en una iteracion posterior.
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
