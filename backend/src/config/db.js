const { Pool } = require('pg');
const { DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME } = require('./env');

/**
 * Pool de conexiones a PostgreSQL (driver `pg`), configurado por
 * completo desde variables de entorno (ver .env.example). Se reutiliza
 * el mismo pool en toda la app: al ser un modulo de Node, el cache de
 * `require` garantiza una unica instancia (patron Singleton).
 *
 * Tablas esperadas (ver docs/mathquest_init_schema.sql):
 *   usuarios(id_usuario, nombre, email, password_hash, rol,
 *            datos_biometricos_activos)
 *   sesiones(id_sesion, id_usuario, token_jwt, fecha_expiracion)
 *   progreso(id_registro, id_usuario, nivel_alcanzado, puntaje,
 *            fecha_actualizacion)
 *
 * NOTA sobre portabilidad: el esquema SQL es portable entre PostgreSQL
 * y MySQL, pero este modulo usa especificamente `pg` (PostgreSQL). Si
 * en el futuro se necesita soportar MySQL en produccion, se puede crear
 * un modulo equivalente con `mysql2/promise` (ya esta en las
 * dependencias del package.json) detras de la misma interfaz basica
 * (pool.query(text, params)).
 */
const pool = new Pool({
  host: DB_HOST,
  port: DB_PORT,
  user: DB_USER,
  password: DB_PASS,
  database: DB_NAME,
});

pool.on('error', (err) => {
  // Los pools de `pg` emiten este evento para errores en conexiones
  // ociosas (ej. la BD se cae); lo registramos para no dejarlo
  // silencioso, pero no tumbamos el proceso.
  console.error('Error inesperado en el pool de PostgreSQL:', err);
});

module.exports = pool;
