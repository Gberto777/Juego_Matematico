const jwt = require('jsonwebtoken');
const { JWT_SECRET, JWT_EXPIRES_IN } = require('../config/env');

/**
 * POST /api/auth/login
 *
 * MOCK: todavia no valida contra la base de datos real (tabla
 * `usuarios` en docs/mathquest_init_schema.sql). Acepta cualquier
 * email/password no vacios y devuelve un usuario simulado + un JWT
 * real (firmado), solo para que la app Android tenga un contrato de
 * API estable contra el que integrar mientras se conecta el SQL real.
 *
 * IMPORTANTE: las claves del JSON de respuesta deben coincidir EXACTO
 * con @SerializedName en
 * app/src/main/java/com/mathquest/model/LoginResponse.kt:
 *   id_usuario, nombre, rol, token_jwt
 *
 * TODO: sustituir el mock por una consulta real a `usuarios` + la
 * comparacion de `password_hash` con bcrypt.compare().
 */
function login(req, res) {
  const { email, password } = req.body || {};

  if (!email || !password) {
    return res.status(400).json({
      error: 'El correo y la contraseña son obligatorios.',
    });
  }

  // Usuario simulado (coincide con uno de los usuarios de prueba
  // insertados en docs/mathquest_init_schema.sql).
  const mockUser = {
    id_usuario: '11d7bb84-7003-4ba2-a584-ad230c560c69',
    nombre: 'Ana Torres',
    rol: 'alumno',
  };

  const tokenJwt = jwt.sign(
    { sub: mockUser.id_usuario, email },
    JWT_SECRET,
    { expiresIn: JWT_EXPIRES_IN }
  );

  return res.status(200).json({
    id_usuario: mockUser.id_usuario,
    nombre: mockUser.nombre,
    rol: mockUser.rol,
    token_jwt: tokenJwt,
  });
}

module.exports = { login };
