const express = require('express');
const router = express.Router();
const { upload, sendMail, downloadAttachment } = require('../controllers/files');
const authenticateToken = require('../middleware/auth');

// POST: send email with attachments
router.post('/send', authenticateToken, upload.array('attachments', 10), sendMail);

// GET: download attachment by filename
router.get('/attachments/:filename', authenticateToken, downloadAttachment);

module.exports = router;