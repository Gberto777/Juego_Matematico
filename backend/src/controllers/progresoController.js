const crypto = require('crypto');
const pool = require('../config/db');

const esEntero = (n) => Number.isInteger(n);

function validarNivelYPuntaje(nivel_alcanzado, puntaje) {
  const nivelNum = Number(nivel_alcanzado);
  const puntajeNum = Number(puntaje);

  const esValido =
    nivel_alcanzado !== undefined &&
    puntaje !== undefined &&
    esEntero(nivelNum) &&
    esEntero(puntajeNum) &&
    nivelNum >= 1 &&
    puntajeNum >= 0;

  return { esValido, nivelNum, puntajeNum };
}

function serializarProgreso(progreso) {
  return {
    id_registro: progreso.id_registro,
    id_usuario: progreso.id_usuario,
    nivel_alcanzado: progreso.nivel_alcanzado,
    puntaje: progreso.puntaje,
    fecha_actualizacion: new Date(progreso.fecha_actualizacion).toISOString(),
  };
}

/**
 * GET /api/progreso/:id
 *
 * Consulta la tabla `progreso` real (docs/mathquest_init_schema.sql)
 * filtrando por id_usuario y devolviendo TODO el historial (no solo el
 * mas reciente), ordenado por fecha_actualizacion DESCENDENTE. Requiere
 * JWT valido (ver src/middleware/authMiddleware.js).
 *
 * Responde 200 con un ARRAY (posiblemente vacio si el usuario aun no
 * tiene progreso) de objetos con las mismas claves que espera la app
 * Android (@SerializedName en ProgresoResponse.kt):
 *   id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion
 */
async function getProgreso(req, res) {
  const { id } = req.params;

  try {
    const { rows } = await pool.query(
      `SELECT id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion
       FROM progreso
       WHERE id_usuario = $1
       ORDER BY fecha_actualizacion DESC`,
      [id]
    );

    return res.status(200).json(rows.map(serializarProgreso));
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
 * Responde 201 con el registro creado.
 */
async function crearProgreso(req, res) {
  const idUsuario = req.user && req.user.sub;
  const { nivel_alcanzado, puntaje } = req.body || {};

  if (!idUsuario) {
    return res.status(401).json({ error: 'Token inválido: falta el usuario.' });
  }

  const { esValido, nivelNum, puntajeNum } = validarNivelYPuntaje(nivel_alcanzado, puntaje);
  if (!esValido) {
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

    return res.status(201).json(serializarProgreso(rows[0]));
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

/**
 * PUT /api/progreso/:id_registro
 *
 * Actualiza nivel_alcanzado y puntaje de un registro existente.
 * Requiere JWT valido. La condicion `WHERE id_registro = $x AND
 * id_usuario = $y` garantiza que un usuario SOLO pueda modificar sus
 * propios registros: si el id_registro existe pero pertenece a otro
 * usuario, la consulta no actualiza ninguna fila y se responde 404
 * (el mismo codigo que si el id_registro no existiera en absoluto, para
 * no revelar si el registro de otro usuario existe).
 *
 * Body esperado: { "nivel_alcanzado": number, "puntaje": number }.
 */
async function actualizarProgreso(req, res) {
  const { id_registro } = req.params;
  const idUsuario = req.user && req.user.sub;
  const { nivel_alcanzado, puntaje } = req.body || {};

  if (!idUsuario) {
    return res.status(401).json({ error: 'Token inválido: falta el usuario.' });
  }

  const { esValido, nivelNum, puntajeNum } = validarNivelYPuntaje(nivel_alcanzado, puntaje);
  if (!esValido) {
    return res.status(400).json({
      error: 'nivel_alcanzado (entero ≥ 1) y puntaje (entero ≥ 0) son obligatorios.',
    });
  }

  try {
    const { rows } = await pool.query(
      `UPDATE progreso
       SET nivel_alcanzado = $1, puntaje = $2, fecha_actualizacion = CURRENT_TIMESTAMP
       WHERE id_registro = $3 AND id_usuario = $4
       RETURNING id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion`,
      [nivelNum, puntajeNum, id_registro, idUsuario]
    );

    const progreso = rows[0];
    if (!progreso) {
      return res.status(404).json({
        error: 'No se encontró un registro de progreso con ese id para tu usuario.',
      });
    }

    return res.status(200).json(serializarProgreso(progreso));
  } catch (error) {
    if (error.code === '23514') {
      return res.status(400).json({ error: 'nivel_alcanzado o puntaje fuera de rango.' });
    }
    console.error('Error en PUT /api/progreso/:id_registro:', error);
    return res.status(500).json({ error: 'Error interno del servidor.' });
  }
}

/**
 * DELETE /api/progreso/:id_registro
 *
 * Elimina un registro existente. Requiere JWT valido. Igual que en
 * [actualizarProgreso], la condicion `WHERE id_registro = $x AND
 * id_usuario = $y` impide borrar registros de otros usuarios; si no se
 * borra ninguna fila, se responde 404.
 *
 * Responde 204 (sin body) si se elimino correctamente.
 */
async function eliminarProgreso(req, res) {
  const { id_registro } = req.params;
  const idUsuario = req.user && req.user.sub;

  if (!idUsuario) {
    return res.status(401).json({ error: 'Token inválido: falta el usuario.' });
  }

  try {
    const { rowCount } = await pool.query(
      'DELETE FROM progreso WHERE id_registro = $1 AND id_usuario = $2',
      [id_registro, idUsuario]
    );

    if (rowCount === 0) {
      return res.status(404).json({
        error: 'No se encontró un registro de progreso con ese id para tu usuario.',
      });
    }

    return res.status(204).send();
  } catch (error) {
    console.error('Error en DELETE /api/progreso/:id_registro:', error);
    return res.status(500).json({ error: 'Error interno del servidor.' });
  }
}

module.exports = { getProgreso, crearProgreso, actualizarProgreso, eliminarProgreso };
