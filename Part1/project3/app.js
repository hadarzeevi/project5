const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const path = require('path');

const app = express();

// MongoDB Connection
const connectDB = async () => {
  try {
  await mongoose.connect('mongodb://host.docker.internal:27017/gmail');
    console.log('MongoDB connected successfully');
  } catch (error) {
    console.error('MongoDB connection error:', error);
    process.exit(1);
  }
};

// Connect to MongoDB
connectDB();

// CORS Configuration
app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true,
  methods: ['GET', 'POST', 'PATCH', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'user-id']
}));

// Middleware
app.use(express.json());

// Routes
const usersU = require('./routes/users');
const usersT = require('./routes/tokens');
const mails = require('./routes/mails');
const blacklist = require('./routes/blacklist');
const labels = require('./routes/labels');
const filesRoutes = require('./routes/files');

app.use('/api/users', usersU);
app.use('/api/tokens', usersT);
app.use('/api/mails', mails);
//app.use('/api/emails', mails);
app.use('/api/labels', labels);
app.use('/api/blacklist', blacklist);

app.disable('etag');

// Static files for uploaded files
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use('/api/files', filesRoutes);

// Error handling middleware
app.use((error, req, res, next) => {
  console.error('Error:', error);
  res.status(500).json({ error: 'Internal server error' });
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error('Unhandled Promise Rejection:', err);
  process.exit(1);
});

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
  process.exit(1);
});

const PORT = 4000;

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});