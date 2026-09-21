const crypto = require('crypto');
const pool = require('../config/db');

/**
 * GET /api/progreso/:id
 *
 * Consulta la tabla `progreso` real (docs/mathquest_init_schema.sql)
 * filtrando por id_usuario y devolviendo el registro mas reciente
 * (fecha_actualizacion DESC), ya que un usuario puede tener varios
 * registros historicos. Requiere JWT valido (ver
 * src/middleware/authMiddleware.js, aplicado en progresoRoutes.js).
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

/**
 * POST /api/progreso
 *
 * Inserta un nuevo registro en la tabla `progreso`. Requiere JWT valido;
 * el `id_usuario` se toma SIEMPRE de `req.user.sub` (el usuario
 * autenticado por el token), nunca del body, para que un usuario no
 * pueda registrar progreso a nombre de otro.
 *
 * Body esperado: { "nivel_alcanzado": number, "puntaje": number }
 * (coincide con ProgresoRequest.kt del lado Android).
 *
 * Responde 201 con el registro creado, usando las mismas claves que
 * ProgresoResponse.kt: id_registro, id_usuario, nivel_alcanzado,
 * puntaje, fecha_actualizacion.
 */
async function crearProgreso(req, res) {
  const idUsuario = req.user && req.user.sub;
  const { nivel_alcanzado, puntaje } = req.body || {};

  if (!idUsuario) {
    return res.status(401).json({ error: 'Token inválido: falta el usuario.' });
  }

  const nivelNum = Number(nivel_alcanzado);
  const puntajeNum = Number(puntaje);

  const esEntero = (n) => Number.isInteger(n);

  if (
    nivel_alcanzado === undefined ||
    puntaje === undefined ||
    !esEntero(nivelNum) ||
    !esEntero(puntajeNum) ||
    nivelNum < 1 ||
    puntajeNum < 0
  ) {
    return res.status(400).json({
      error: 'nivel_alcanzado (entero ≥ 1) y puntaje (entero ≥ 0) son obligatorios.',
    });
  }

  const idRegistro = crypto.randomUUID();

  try {
    const { rows } = await pool.query(
      `INSERT INTO progreso (id_registro, id_usuario, nivel_alcanzado, puntaje)
       VALUES ($1, $2, $3, $4)
       RETURNING id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion`,
      [idRegistro, idUsuario, nivelNum, puntajeNum]
    );
    const progreso = rows[0];

    return res.status(201).json({
      id_registro: progreso.id_registro,
      id_usuario: progreso.id_usuario,
      nivel_alcanzado: progreso.nivel_alcanzado,
      puntaje: progreso.puntaje,
      fecha_actualizacion: new Date(progreso.fecha_actualizacion).toISOString(),
    });
  } catch (error) {
    // 23503 = foreign_key_violation: el id_usuario del token no existe en `usuarios`.
    if (error.code === '23503') {
      return res.status(400).json({ error: 'El usuario del token no existe.' });
    }
    // 23514 = check_violation: nivel_alcanzado/puntaje violan el CHECK de la tabla.
    if (error.code === '23514') {
      return res.status(400).json({ error: 'nivel_alcanzado o puntaje fuera de rango.' });
    }
    console.error('Error en POST /api/progreso:', error);
    return res.status(500).json({ error: 'Error interno del servidor.' });
  }
}

module.exports = { getProgreso, crearProgreso };
