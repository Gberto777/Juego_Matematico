-- =============================================================================
-- MathQuest (Juego_Matematico) - Script de inicializacion de base de datos
-- =============================================================================
-- Motor destino: PORTABLE entre PostgreSQL (>= 12) y MySQL (>= 8.0) / MariaDB (>= 10.2)
--
-- Decisiones de portabilidad (resumen; el detalle esta comentado junto a cada
-- linea afectada):
--   1. Claves primarias como UUID representado en VARCHAR(36).
--      - Evita la incompatibilidad AUTO_INCREMENT (MySQL) vs SERIAL/IDENTITY
--        (PostgreSQL), que no tiene una sintaxis comun.
--      - Los valores se generan en la aplicacion o, si el motor lo soporta,
--        con una funcion nativa (ver alternativas comentadas mas abajo).
--   2. BOOLEAN funciona en ambos motores. En MySQL/MariaDB es un alias de
--      TINYINT(1); en PostgreSQL es un tipo nativo. La sintaxis es idéntica.
--   3. TIMESTAMP con DEFAULT CURRENT_TIMESTAMP es SQL estandar y se comporta
--      igual en ambos motores.
--   4. Las restricciones CHECK se soportan en PostgreSQL desde siempre y en
--      MySQL >= 8.0.16 / MariaDB >= 10.2 (antes se aceptaban pero se
--      ignoraban silenciosamente). Se documenta igualmente por si el
--      objetivo es una version mas antigua de MySQL.
--   5. Los comentarios usan "--" (soportado por ambos motores) en vez de "#"
--      (solo MySQL) o bloques /* */ para mantener un unico estilo.
--   6. Los indices sobre columnas de clave foranea se crean explicitamente
--      con CREATE INDEX porque PostgreSQL NO los crea automaticamente al
--      definir una FOREIGN KEY (MySQL/InnoDB si lo hace, pero crear el
--      indice explicito de todas formas es válido y no genera error).
--
-- Validado localmente ejecutando el script completo (DDL + DML) contra:
--   - PostgreSQL 16 (paquete oficial de Ubuntu 24.04)
--   - MariaDB 10.11 (compatible con la sintaxis estandar de MySQL 8 usada aqui)
-- =============================================================================


-- -----------------------------------------------------------------------------
-- Tabla: usuarios
-- -----------------------------------------------------------------------------
-- id_usuario: UUID almacenado como VARCHAR(36) (formato
--   "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"), generado por la aplicacion.
--
--   Alternativas especificas de motor (no usadas aqui por portabilidad):
--     * PostgreSQL: id_usuario UUID PRIMARY KEY DEFAULT gen_random_uuid();
--                   (requiere la extension pgcrypto: CREATE EXTENSION IF
--                   NOT EXISTS pgcrypto;) o DEFAULT uuid_generate_v4()
--                   con la extension "uuid-ossp".
--     * MySQL/MariaDB: id_usuario CHAR(36) PRIMARY KEY DEFAULT (UUID());
--                   (DEFAULT con expresion soportado desde MySQL 8.0.13).
--     * Alternativa entera autoincremental (mas simple pero NO portable
--       sin reescritura):
--         - PostgreSQL: id_usuario SERIAL PRIMARY KEY  (o BIGSERIAL)
--         - MySQL:      id_usuario INT AUTO_INCREMENT PRIMARY KEY
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario                  VARCHAR(36)  NOT NULL,
    nombre                      VARCHAR(150) NOT NULL,
    email                       VARCHAR(255) NOT NULL,
    password_hash               VARCHAR(255) NOT NULL,
    rol                         VARCHAR(20)  NOT NULL DEFAULT 'alumno',
    datos_biometricos_activos   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    -- Restringe los roles válidos del sistema; ampliar la lista si se
    -- agregan nuevos perfiles (ej. 'administrador').
    CONSTRAINT chk_usuarios_rol CHECK (rol IN ('alumno', 'profesor', 'administrador'))
);


-- -----------------------------------------------------------------------------
-- Tabla: sesiones
-- -----------------------------------------------------------------------------
-- Registra las sesiones activas/emitidas para cada usuario (p. ej. tokens
-- JWT emitidos al iniciar sesion).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sesiones (
    id_sesion          VARCHAR(36)  NOT NULL,
    id_usuario         VARCHAR(36)  NOT NULL,
    -- 512 caracteres es holgado para un JWT firmado con HS256/RS256 tipico.
    token_jwt          VARCHAR(512) NOT NULL,
    fecha_expiracion   TIMESTAMP    NOT NULL,
    CONSTRAINT pk_sesiones PRIMARY KEY (id_sesion),
    CONSTRAINT fk_sesiones_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- PostgreSQL no indexa automaticamente las columnas de FK; se crea el
-- indice explicitamente para acelerar los JOIN/lookup por usuario.
-- En MySQL/MariaDB este indice puede coexistir con el que InnoDB crea
-- automaticamente para la FK sin generar error.
CREATE INDEX idx_sesiones_id_usuario ON sesiones (id_usuario);


-- -----------------------------------------------------------------------------
-- Tabla: progreso
-- -----------------------------------------------------------------------------
-- Guarda el avance del usuario dentro del juego (nivel alcanzado y puntaje).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS progreso (
    id_registro          VARCHAR(36) NOT NULL,
    id_usuario            VARCHAR(36) NOT NULL,
    nivel_alcanzado        INT         NOT NULL DEFAULT 1,
    puntaje                INT         NOT NULL DEFAULT 0,
    fecha_actualizacion    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_progreso PRIMARY KEY (id_registro),
    CONSTRAINT fk_progreso_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_progreso_nivel CHECK (nivel_alcanzado >= 1),
    CONSTRAINT chk_progreso_puntaje CHECK (puntaje >= 0)
);

CREATE INDEX idx_progreso_id_usuario ON progreso (id_usuario);


-- =============================================================================
-- DML: datos de prueba
-- =============================================================================
-- Los hashes de password_hash son valores de ejemplo con formato bcrypt
-- ($2b$12$...) puramente ilustrativos (NO corresponden a una contrasena real
-- utilizable); en produccion deben generarse con una libreria de hashing
-- (bcrypt/argon2) del lado de la aplicacion, nunca en SQL plano.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 3 usuarios de prueba: 1 alumno, 1 profesor, 1 alumno con biometria activa.
-- -----------------------------------------------------------------------------
INSERT INTO usuarios (id_usuario, nombre, email, password_hash, rol, datos_biometricos_activos)
VALUES
    ('11d7bb84-7003-4ba2-a584-ad230c560c69', 'Ana Torres',     'ana.torres@mathquest.edu',     '$2b$12$KIXQeYUJfB3n9hR1V8t6UOo1lJj0m3q9WqvYQ3z8vXk3nQdG9m9O6', 'alumno',   TRUE),
    ('57db825b-6ecc-463e-91b5-30c3a4972fc7', 'Carlos Medina',  'carlos.medina@mathquest.edu',  '$2b$12$M0LiYh5xI2wS8pQ2s1E4uOZq7Rk8T3nB1yV6xC2f4Hj0aP5eD9G3q', 'alumno',   FALSE),
    ('2c7925fc-aa9c-4a16-966b-5b8336ef9383', 'Laura Fernandez','laura.fernandez@mathquest.edu','$2b$12$T7nQ1z3xR8mK4vL9bC0eYuHp2Wj5S6dF1oN8gA3iE7cV0qX4rZ2Ky', 'profesor', FALSE);

-- -----------------------------------------------------------------------------
-- Sesiones de ejemplo (una sesion activa por cada usuario).
-- fecha_expiracion se fija a una fecha fija en el futuro para que los datos
-- de ejemplo sean deterministas y reproducibles en cualquier motor/zona
-- horaria; en la aplicacion real se calcularia como "ahora + duracion".
-- -----------------------------------------------------------------------------
INSERT INTO sesiones (id_sesion, id_usuario, token_jwt, fecha_expiracion)
VALUES
    ('8a606944-37a7-41dc-acd2-b52d6177cac3', '11d7bb84-7003-4ba2-a584-ad230c560c69',
     'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEudG9ycmVzIn0.ejemplo_firma_ana',   '2026-12-31 23:59:59'),
    ('c433e855-a76b-4ff4-8247-77f431de39c6', '57db825b-6ecc-463e-91b5-30c3a4972fc7',
     'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjYXJsb3MubWVkaW5hIn0.ejemplo_firma_carlos', '2026-12-31 23:59:59'),
    ('aec8bd9a-f30c-43aa-9223-cefe750e2ec6', '2c7925fc-aa9c-4a16-966b-5b8336ef9383',
     'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsYXVyYS5mZXJuYW5kZXoifQ.ejemplo_firma_laura', '2026-12-31 23:59:59');

-- -----------------------------------------------------------------------------
-- Registros de progreso de ejemplo (uno por usuario; Ana tiene dos registros
-- historicos para ilustrar el avance de nivel/puntaje a lo largo del tiempo).
-- -----------------------------------------------------------------------------
INSERT INTO progreso (id_registro, id_usuario, nivel_alcanzado, puntaje, fecha_actualizacion)
VALUES
    ('0e4dd50b-1e4f-4266-ab14-3a85e2ead132', '11d7bb84-7003-4ba2-a584-ad230c560c69', 3, 450, '2026-09-10 10:15:00'),
    ('de53531f-2758-4f86-a8e3-3cf89ca32b0a', '11d7bb84-7003-4ba2-a584-ad230c560c69', 5, 980, '2026-09-18 16:40:00'),
    ('9f0572b7-178b-4a18-a4c7-3f4e15bf5010', '57db825b-6ecc-463e-91b5-30c3a4972fc7', 1, 60,  '2026-09-15 09:05:00');
