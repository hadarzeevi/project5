const mongoose = require('mongoose');

const emailSchema = new mongoose.Schema({
  ownerId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  from: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  fromMail: {
    type: String,
    required: true
  },
  to: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  toMail: {
    type: String,
    default: ''
  },
  subject: {
    type: String,
    required: true
  },
  body: {
    type: String,
    required: true
  },
  timestamp: {
    type: Date,
    default: Date.now
  },
  preview: {
    type: String,
    default: ''
  },
  isRead: {
    type: Boolean,
    default: false
  },
  category: {
    type: String,
    enum: ['inbox', 'sent', 'drafts', 'spam', 'trash'],
    default: 'inbox'
  },
  previousCategory: {
    type: String,
    enum: ['inbox', 'sent', 'drafts', 'spam', 'trash'],
    default: 'inbox'
  },
  isStarred: {
    type: Boolean,
    default: false
  },
  labelId: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Label'
  }],
  label: [{
    type: String
  }],
  link: {
    type: String,
    default: ''
  },

  attachments: {
    type: [{
      filename:     { type: String, required: true },
      originalName: { type: String, required: true },
      size:         { type: Number, required: true },
      contentType:     { type: String, required: true },
      path:         { type: String, required: true }
    }],
    default: []
  }

}, {
  timestamps: true
});

emailSchema.index({ ownerId: 1, timestamp: -1 });
emailSchema.index({ from: 1, to: 1 });
emailSchema.index({ category: 1 });
emailSchema.index({ subject: 'text', body: 'text' });

const Email = mongoose.model('Email', emailSchema);

module.exports = Email;
