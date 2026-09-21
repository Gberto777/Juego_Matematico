const express = require('express');
const { getProgreso } = require('../controllers/progresoController');

const router = express.Router();

// GET /api/progreso/:id
router.get('/:id', getProgreso);

module.exports = router;
