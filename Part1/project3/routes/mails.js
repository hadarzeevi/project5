const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const mailsController = require('../controllers/mails');

// Get latest 50 emails for user with pagination
router.get('/', auth, mailsController.get50Emails);

// Search emails by query
router.get('/search/:query', auth, mailsController.searchEmails);

// Get email by ID
router.get('/:id', auth, mailsController.getMail);

// Delete email by ID
router.delete('/:id', auth, mailsController.deleteMail);

// Update email by ID
router.patch('/:id', auth, mailsController.patchMail);

// Send a new email
router.post('/', auth, mailsController.postMail);

// Toggle label on/off for a specific mail
router.patch('/:id/labels', auth, mailsController.toggleLabelOnMail);

module.exports = router;