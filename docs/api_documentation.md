# API REST de MathQuest — Documentación técnica

Backend Node.js/Express (`/backend`) que da servicio a la app Android de
MathQuest. Todas las rutas están montadas bajo `http://<host>:3000/`.

- **Host de desarrollo** (emulador de Android): `http://10.0.2.2:3000/`
- **Formato**: JSON (`Content-Type: application/json`) en request y response.
- **Base de datos**: PostgreSQL, esquema en
  [`docs/mathquest_init_schema.sql`](./mathquest_init_schema.sql)
  (tablas `usuarios`, `sesiones`, `progreso`).

## Autenticación

Todos los endpoints de `/api/progreso/*` requieren un JWT válido, emitido
por `POST /api/auth/login`, enviado en el header estándar:

```
Authorization: Bearer <token_jwt>
```

El middleware de autenticación (`backend/src/middleware/authMiddleware.js`):

- Responde **401** si falta el header, no usa el esquema `Bearer`, o el
  token es inválido/expiró.
- Si es válido, expone el `id_usuario` autenticado como `req.user.sub`
  a los controladores (nunca se confía en un `id_usuario` enviado por
  el cliente en el body).

`POST /api/auth/login` es la única ruta pública (no requiere el header).

---

## `POST /api/auth/login`

Autentica un usuario por email/password y devuelve un JWT.

**Autenticación:** no requerida (esta es la ruta que la genera).

### Request

```json
{
  "email": "ana.torres@mathquest.edu",
  "password": "MathQuest2026!"
}
```

| Campo      | Tipo   | Obligatorio | Notas                                    |
|------------|--------|-------------|-------------------------------------------|
| `email`    | string | sí          | Debe existir en la tabla `usuarios`.      |
| `password` | string | sí          | Texto plano (viaja solo por HTTPS en prod); se compara con `bcrypt.compare` contra `password_hash`. |

### Response — 200 OK

```json
{
  "id_usuario": "11d7bb84-7003-4ba2-a584-ad230c560c69",
  "nombre": "Ana Torres",
  "rol": "alumno",
  "token_jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Efecto secundario: se inserta una fila en la tabla `sesiones`
(`id_sesion`, `id_usuario`, `token_jwt`, `fecha_expiracion`).

### Errores

| Código | Cuándo                                              | Body                                              |
|--------|------------------------------------------------------|----------------------------------------------------|
| 400    | Falta `email` o `password` en el body                | `{"error": "El correo y la contraseña son obligatorios."}` |
| 401    | Email no registrado, o password incorrecto (mismo mensaje para ambos casos, por seguridad) | `{"error": "Credenciales inválidas."}` |
| 500    | Error inesperado del servidor/base de datos          | `{"error": "Error interno del servidor."}`         |

---

## `GET /api/progreso/:id`

Devuelve **todo el historial** de progreso de un usuario (no solo el
registro más reciente), ordenado por `fecha_actualizacion` descendente.

**Autenticación:** requerida (`Authorization: Bearer <token>`).

### Parámetros de ruta

| Parámetro | Tipo   | Descripción                          |
|-----------|--------|---------------------------------------|
| `id`      | string | `id_usuario` cuyo historial se pide.  |

### Response — 200 OK

Array (posiblemente vacío si el usuario aún no tiene progreso registrado):

```json
[
  {
    "id_registro": "de53531f-2758-4f86-a8e3-3cf89ca32b0a",
    "id_usuario": "11d7bb84-7003-4ba2-a584-ad230c560c69",
    "nivel_alcanzado": 7,
    "puntaje": 1500,
    "fecha_actualizacion": "2026-09-21T14:41:57.596Z"
  },
  {
    "id_registro": "0e4dd50b-1e4f-4266-ab14-3a85e2ead132",
    "id_usuario": "11d7bb84-7003-4ba2-a584-ad230c560c69",
    "nivel_alcanzado": 3,
    "puntaje": 450,
    "fecha_actualizacion": "2026-09-10T10:15:00.000Z"
  }
]
```

### Errores

| Código | Cuándo                                   | Body                                         |
|--------|-------------------------------------------|-----------------------------------------------|
| 401    | Falta el token, o es inválido/expiró      | `{"error": "Token de autenticación requerido."}` o `{"error": "Token inválido o expirado."}` |
| 500    | Error inesperado                          | `{"error": "Error interno del servidor."}`    |

---

## `POST /api/progreso`

Registra un nuevo avance (nivel + puntaje) para el usuario autenticado.

**Autenticación:** requerida. El `id_usuario` del nuevo registro se toma
**siempre** de `req.user.sub` (el token), nunca del body — un usuario no
puede registrar progreso a nombre de otro.

### Request

```json
{
  "nivel_alcanzado": 6,
  "puntaje": 1200
}
```

| Campo             | Tipo | Obligatorio | Validación                     |
|-------------------|------|-------------|----------------------------------|
| `nivel_alcanzado` | int  | sí          | Entero ≥ 1 (`CHECK` en la BD).   |
| `puntaje`         | int  | sí          | Entero ≥ 0 (`CHECK` en la BD).   |

### Response — 201 Created

```json
{
  "id_registro": "b3e485c1-6a82-42a2-9655-b2bd4bf54078",
  "id_usuario": "11d7bb84-7003-4ba2-a584-ad230c560c69",
  "nivel_alcanzado": 6,
  "puntaje": 1200,
  "fecha_actualizacion": "2026-09-21T14:32:18.052Z"
}
```

### Errores

| Código | Cuándo                                                       | Body                                                                      |
|--------|-----------------------------------------------------------------|----------------------------------------------------------------------------|
| 400    | `nivel_alcanzado`/`puntaje` ausentes, no enteros, o fuera de rango | `{"error": "nivel_alcanzado (entero ≥ 1) y puntaje (entero ≥ 0) son obligatorios."}` |
| 401    | Falta el token, es inválido/expiró, o no trae usuario           | `{"error": "..."}` (ver tabla de `authMiddleware`)                        |
| 500    | Error inesperado                                                 | `{"error": "Error interno del servidor."}`                                |

---

## `PUT /api/progreso/:id_registro`

Actualiza `nivel_alcanzado` y `puntaje` de un registro existente.

**Autenticación:** requerida. La actualización se ejecuta con
`WHERE id_registro = :id_registro AND id_usuario = <token>`: si el
registro existe pero pertenece a **otro** usuario, no se actualiza
ninguna fila y se responde **404** (el mismo código que si el
`id_registro` no existiera en absoluto — para no revelar si el registro
de otro usuario existe).

### Parámetros de ruta

| Parámetro     | Tipo   | Descripción                        |
|---------------|--------|--------------------------------------|
| `id_registro` | string | `id_registro` del progreso a editar. |

### Request

```json
{
  "nivel_alcanzado": 7,
  "puntaje": 1500
}
```

Mismas reglas de validación que `POST /api/progreso`.

### Response — 200 OK

```json
{
  "id_registro": "de53531f-2758-4f86-a8e3-3cf89ca32b0a",
  "id_usuario": "11d7bb84-7003-4ba2-a584-ad230c560c69",
  "nivel_alcanzado": 7,
  "puntaje": 1500,
  "fecha_actualizacion": "2026-09-21T14:41:57.596Z"
}
```

`fecha_actualizacion` se recalcula en el servidor (`CURRENT_TIMESTAMP`),
no se acepta desde el cliente.

### Errores

| Código | Cuándo                                                              | Body                                                                        |
|--------|------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| 400    | `nivel_alcanzado`/`puntaje` inválidos                                   | `{"error": "nivel_alcanzado (entero ≥ 1) y puntaje (entero ≥ 0) son obligatorios."}` |
| 401    | Falta el token, es inválido/expiró                                      | `{"error": "..."}`                                                          |
| 404    | El `id_registro` no existe, **o pertenece a otro usuario**             | `{"error": "No se encontró un registro de progreso con ese id para tu usuario."}` |
| 500    | Error inesperado                                                        | `{"error": "Error interno del servidor."}`                                  |

---

## `DELETE /api/progreso/:id_registro`

Elimina un registro existente.

**Autenticación:** requerida. Mismo criterio de propiedad que `PUT`:
`WHERE id_registro = :id_registro AND id_usuario = <token>`; si no se
borra ninguna fila (no existe o no es del usuario), responde **404**.

### Parámetros de ruta

| Parámetro     | Tipo   | Descripción                          |
|---------------|--------|----------------------------------------|
| `id_registro` | string | `id_registro` del progreso a eliminar. |

### Response — 204 No Content

Sin body.

### Errores

| Código | Cuándo                                                  | Body                                                                        |
|--------|------------------------------------------------------------|-------------------------------------------------------------------------------|
| 401    | Falta el token, es inválido/expiró                          | `{"error": "..."}`                                                          |
| 404    | El `id_registro` no existe, **o pertenece a otro usuario**  | `{"error": "No se encontró un registro de progreso con ese id para tu usuario."}` |
| 500    | Error inesperado                                            | `{"error": "Error interno del servidor."}`                                  |

---

## Resumen de endpoints

| Método   | Ruta                        | Auth | Body                                        | Respuesta exitosa            |
|----------|-----------------------------|------|----------------------------------------------|-------------------------------|
| `POST`   | `/api/auth/login`           | No   | `{email, password}`                          | `200` usuario + `token_jwt`   |
| `GET`    | `/api/progreso/:id`         | Sí   | —                                             | `200` array de progreso       |
| `POST`   | `/api/progreso`              | Sí   | `{nivel_alcanzado, puntaje}`                 | `201` registro creado         |
| `PUT`    | `/api/progreso/:id_registro`| Sí   | `{nivel_alcanzado, puntaje}`                 | `200` registro actualizado    |
| `DELETE` | `/api/progreso/:id_registro`| Sí   | —                                             | `204` sin body                |

## Correspondencia con la app Android

| Endpoint                        | Cliente Retrofit (`MathQuestApiService.kt`) | Modelo Kotlin                          |
|----------------------------------|----------------------------------------------|------------------------------------------|
| `POST /api/auth/login`           | `login(LoginRequest): Response<LoginResponse>` | `LoginRequest`, `LoginResponse`         |
| `GET /api/progreso/{id}`         | `getProgreso(id): Response<List<ProgresoResponse>>` | `ProgresoResponse`                 |
| `POST /api/progreso`             | `crearProgreso(ProgresoRequest): Response<ProgresoResponse>` | `ProgresoRequest`, `ProgresoResponse` |
| `PUT /api/progreso/{id}`         | `actualizarProgreso(id, ProgresoRequest): Response<ProgresoResponse>` | `ProgresoRequest`, `ProgresoResponse` |
| `DELETE /api/progreso/{id}`      | `eliminarProgreso(id): Response<Unit>`       | —                                        |

El header `Authorization: Bearer <token>` se inyecta automáticamente en
todas las peticiones desde el cliente Android mediante un `Interceptor`
de OkHttp configurado en `RetrofitClient.kt`, que lee el token
persistido de forma cifrada en `SessionManager` (`EncryptedSharedPreferences`).
