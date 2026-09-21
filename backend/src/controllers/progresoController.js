/**
 * GET /api/progreso/:id
 *
 * MOCK: todavia no consulta la tabla `progreso` real (ver
 * docs/mathquest_init_schema.sql). Devuelve datos de ejemplo para que
 * la app Android tenga un contrato de API estable contra el que
 * integrar mientras se conecta el SQL real.
 *
 * IMPORTANTE: las claves del JSON de respuesta deben coincidir EXACTO
 * con @SerializedName en
 * app/src/main/java/com/mathquest/model/ProgresoResponse.kt:
 *   id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion
 *
 * TODO: sustituir el mock por un SELECT real sobre `progreso` filtrando
 * por id_usuario (y probablemente devolviendo el registro mas reciente).
 */
function getProgreso(req, res) {
  const { id } = req.params;

  const mockProgreso = {
    id_registro: 'de53531f-2758-4f86-a8e3-3cf89ca32b0a',
    id_usuario: id,
    nivel_alcanzado: 5,
    puntaje: 980,
    fecha_actualizacion: '2026-09-18T16:40:00.000Z',
  };

  return res.status(200).json(mockProgreso);
}

module.exports = { getProgreso };
