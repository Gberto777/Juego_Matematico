// Configuracion centralizada leida desde variables de entorno (dotenv).
// TODO: agregar aqui la configuracion de conexion a PostgreSQL/MySQL
// (DATABASE_URL, etc.) cuando se implemente el acceso real a la base de
// datos descrita en docs/mathquest_init_schema.sql.

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'mathquest-dev-secret-CHANGE-ME';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '1h';

module.exports = {
  PORT,
  JWT_SECRET,
  JWT_EXPIRES_IN,
};
