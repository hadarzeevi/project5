const express = require ('express')
var router = express.Router()
const path = require('path');
const multer = require('multer');
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/');
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const ext = path.extname(file.originalname);
    cb(null, uniqueSuffix + ext);
  }
});
const upload = multer({ dest: 'uploads/' });


const controller = require('../controllers/users')

router.post('/register', upload.single('profileImage'), controller.addUser);

// POST: create user
router.post('/', controller.addUser);

// GET: get user by ID
router.get('/:id', controller.getUser);

router.put('/:id', upload.single('profileImage'), controller.updateUser);

router.get('/check-username/:username', controller.checkUsername);

module.exports = router
