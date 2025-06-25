const express = require('express')
const router = express.Router()

const labelsController = require('../controllers/labels')
const auth = require('../middleware/auth')

// Get all labels for a user
router.get('/', auth, labelsController.getAllLabels)
// Add a new label
router.post('/', auth, labelsController.addLabel)
// Get a label by ID
router.get('/:id', auth, labelsController.getLabel)
// Edit a label by ID
router.patch('/:id', auth, labelsController.editLabel)
// Delete a label by ID
router.delete('/:id', auth, labelsController.deleteLabel)

module.exports = router
