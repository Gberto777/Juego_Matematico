const express = require('express');
const { getProgreso, crearProgreso } = require('../controllers/progresoController');
const { authenticateToken } = require('../middleware/authMiddleware');

const router = express.Router();

// Todas las rutas de progreso requieren un JWT valido (Authorization: Bearer <token>).
router.use(authenticateToken);

// GET /api/progreso/:id
router.get('/:id', getProgreso);

// POST /api/progreso
router.post('/', crearProgreso);

module.exports = router;
