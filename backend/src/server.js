require('dotenv').config();

const express = require('express');
const cors = require('cors');

const { PORT } = require('./config/env');
const authRoutes = require('./routes/authRoutes');
const progresoRoutes = require('./routes/progresoRoutes');

const app = express();

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.json({ status: 'ok', service: 'mathquest-backend' });
});

app.use('/api/auth', authRoutes);
app.use('/api/progreso', progresoRoutes);

// Manejador simple para rutas no encontradas.
app.use((req, res) => {
  res.status(404).json({ error: 'Recurso no encontrado.' });
});

if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`MathQuest backend escuchando en el puerto ${PORT}`);
  });
}

module.exports = app;
