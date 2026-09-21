const express = require('express');
const {
  getProgreso,
  crearProgreso,
  actualizarProgreso,
  eliminarProgreso,
} = require('../controllers/progresoController');
const { authenticateToken } = require('../middleware/authMiddleware');

const router = express.Router();

// Todas las rutas de progreso requieren un JWT valido (Authorization: Bearer <token>).
router.use(authenticateToken);

// GET /api/progreso/:id -> historial completo del usuario
router.get('/:id', getProgreso);

// POST /api/progreso -> crea un registro nuevo
router.post('/', crearProgreso);

// PUT /api/progreso/:id_registro -> actualiza nivel/puntaje (solo si es del usuario del token)
router.put('/:id_registro', actualizarProgreso);

// DELETE /api/progreso/:id_registro -> elimina (solo si es del usuario del token)
router.delete('/:id_registro', eliminarProgreso);

module.exports = router;
