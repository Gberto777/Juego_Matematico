const crypto = require('crypto');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const pool = require('../config/db');
const { JWT_SECRET, JWT_EXPIRES_IN, SESSION_DURATION_HOURS } = require('../config/env');

/**
 * POST /api/auth/login
 *
 * Valida credenciales contra la tabla `usuarios` real
 * (docs/mathquest_init_schema.sql):
 *   1. SELECT del usuario por email.
 *   2. bcrypt.compare(password, password_hash).
 *   3. Si es valido: INSERT en `sesiones` con un JWT real y responde con
 *      la misma forma de JSON que espera la app Android
 *      (@SerializedName en LoginResponse.kt):
 *        id_usuario, nombre, rol, token_jwt
 */
async function login(req, res) {
  const { email, password } = req.body || {};

  if (!email || !password) {
    return res.status(400).json({
      error: 'El correo y la contraseña son obligatorios.',
    });
  }

  try {
    const { rows } = await pool.query(
      'SELECT id_usuario, nombre, rol, password_hash FROM usuarios WHERE email = $1',
      [email]
    );
    const usuario = rows[0];

    // Se devuelve el mismo mensaje de error tanto si el email no existe
    // como si la contraseña es incorrecta, para no filtrar que emails
    // estan registrados en el sistema.
    if (!usuario) {
      return res.status(401).json({ error: 'Credenciales inválidas.' });
    }

    const passwordValido = await bcrypt.compare(password, usuario.password_hash);
    if (!passwordValido) {
      return res.status(401).json({ error: 'Credenciales inválidas.' });
    }

    const idSesion = crypto.randomUUID();
    const tokenJwt = jwt.sign(
      { sub: usuario.id_usuario, email },
      JWT_SECRET,
      { expiresIn: JWT_EXPIRES_IN }
    );
    const fechaExpiracion = new Date(Date.now() + SESSION_DURATION_HOURS * 60 * 60 * 1000);

    await pool.query(
      `INSERT INTO sesiones (id_sesion, id_usuario, token_jwt, fecha_expiracion)
       VALUES ($1, $2, $3, $4)`,
      [idSesion, usuario.id_usuario, tokenJwt, fechaExpiracion]
    );

    return res.status(200).json({
      id_usuario: usuario.id_usuario,
      nombre: usuario.nombre,
      rol: usuario.rol,
      token_jwt: tokenJwt,
    });
  } catch (error) {
    console.error('Error en POST /api/auth/login:', error);
    return res.status(500).json({ error: 'Error interno del servidor.' });
  }
}

module.exports = { login };
