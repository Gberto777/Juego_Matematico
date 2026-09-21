// Configuracion centralizada leida desde variables de entorno (dotenv).

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'mathquest-dev-secret-CHANGE-ME';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '1h';

// Duracion de la sesion persistida en la tabla `sesiones`
// (fecha_expiracion). Idealmente debe mantenerse alineada con
// JWT_EXPIRES_IN; se expresa por separado en horas porque
// fecha_expiracion se calcula con un Date de JavaScript, no con la
// sintaxis de duracion de jsonwebtoken (ej. "1h", "7d").
const SESSION_DURATION_HOURS = Number(process.env.SESSION_DURATION_HOURS) || 1;

// Conexion a PostgreSQL (driver `pg`), usada por src/config/db.js.
// Ver docs/mathquest_init_schema.sql para el esquema de tablas.
const DB_HOST = process.env.DB_HOST || 'localhost';
const DB_PORT = Number(process.env.DB_PORT) || 5432;
const DB_USER = process.env.DB_USER || 'postgres';
const DB_PASS = process.env.DB_PASS || '';
const DB_NAME = process.env.DB_NAME || 'mathquest';

module.exports = {
  PORT,
  JWT_SECRET,
  JWT_EXPIRES_IN,
  SESSION_DURATION_HOURS,
  DB_HOST,
  DB_PORT,
  DB_USER,
  DB_PASS,
  DB_NAME,
};
