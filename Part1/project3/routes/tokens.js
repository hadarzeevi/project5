const express = require('express')
var router = express.Router()
const controller = require('../controllers/tokens')

// POST: login
router.post('/',controller.findUser)
router.post('/verify-mail', controller.verifyMail);

module.exports = router
