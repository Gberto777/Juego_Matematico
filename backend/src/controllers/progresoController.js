const pool = require('../config/db');

/**
 * GET /api/progreso/:id
 *
 * Consulta la tabla `progreso` real (docs/mathquest_init_schema.sql)
 * filtrando por id_usuario y devolviendo el registro mas reciente
 * (fecha_actualizacion DESC), ya que un usuario puede tener varios
 * registros historicos.
 *
 * El JSON de respuesta usa las mismas claves que espera la app Android
 * (@SerializedName en ProgresoResponse.kt):
 *   id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion
 */
async function getProgreso(req, res) {
  const { id } = req.params;

  try {
    const { rows } = await pool.query(
      `SELECT id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion
       FROM progreso
       WHERE id_usuario = $1
       ORDER BY fecha_actualizacion DESC
       LIMIT 1`,
      [id]
    );
    const progreso = rows[0];

    if (!progreso) {
      return res.status(404).json({
        error: 'No se encontró progreso para este usuario.',
      });
    }

    return res.status(200).json({
      id_registro: progreso.id_registro,
      id_usuario: progreso.id_usuario,
      nivel_alcanzado: progreso.nivel_alcanzado,
      puntaje: progreso.puntaje,
      fecha_actualizacion: new Date(progreso.fecha_actualizacion).toISOString(),
    });
  } catch (error) {
    console.error('Error en GET /api/progreso/:id:', error);
    return res.status(500).json({ error: 'Error interno del servidor.' });
  }
}

module.exports = { getProgreso };
