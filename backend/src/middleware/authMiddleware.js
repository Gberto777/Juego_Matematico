const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../config/env');

/**
 * Middleware de autenticacion: exige un header
 *   Authorization: Bearer <token_jwt>
 * valido (firmado con JWT_SECRET, no expirado). Si es valido, adjunta
 * el payload decodificado a `req.user` (incluye `req.user.sub`, el
 * id_usuario, tal como lo firma authController.login) para que los
 * controladores lo usen sin volver a decodificar el token.
 *
 * Responde 401 si falta el header, el esquema no es "Bearer", o el
 * token es invalido/expirado.
 */
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'] || '';
  const [scheme, token] = authHeader.split(' ');

  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ error: 'Token de autenticación requerido.' });
  }

  jwt.verify(token, JWT_SECRET, (err, payload) => {
    if (err) {
      return res.status(401).json({ error: 'Token inválido o expirado.' });
    }
    req.user = payload;
    next();
  });
}

module.exports = { authenticateToken };
