const mongoose = require('mongoose');

const labelSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true
  },
  color: {
    type: String,
    required: false,
    trim: true
  },
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    required: true,
    index: true
  }
}, {
  timestamps: true // Adds createdAt and updatedAt automatically
});

// Create compound index for unique label names per user
labelSchema.index({ userId: 1, name: 1 }, { unique: true });

module.exports = mongoose.model('Label', labelSchema);