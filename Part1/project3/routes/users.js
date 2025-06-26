const express = require('express');
const router = express.Router();
const path = require('path');
const multer = require('multer');

// Multer configuration for file uploads
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

const upload = multer({ 
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024 // 5MB limit
  },
  fileFilter: function (req, file, cb) {
    // Accept only image files
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Only image files are allowed'));
    }
  }
});

const controller = require('../controllers/users');

// POST: register user with profile image
router.post('/register', upload.single('profileImage'), controller.addUser);

// POST: create user (alternative endpoint)
router.post('/', controller.addUser);

// GET: get user by ID
router.get('/:id', controller.getUser);

// PUT: update user by ID with profile image
router.put('/:id', upload.single('profileImage'), controller.updateUser);

// GET: check if username is unique
router.get('/check-username/:username', controller.checkUsername);

module.exports = router;