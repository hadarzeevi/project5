const Users = require('../models/users');
const jwt = require('jsonwebtoken'); 

const SECRET_KEY = 'secretkeyHHE';

// Authenticate user by email and password
exports.findUser = async (req, res) => {
  try {
    const { mail, password } = req.body;

    const user = await Users.authenticate(mail, password);
    if (user) {
      const token = jwt.sign({ userId: user._id }, SECRET_KEY, { expiresIn: '1h' });
      res.status(200).json({ userId: user._id, token });
    } else {
      res.status(404).json({ error: "Invalid credentials" });
    }
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Verify if email exists
exports.verifyMail = async (req, res) => {
  try {
    const { mail } = req.body;
    const user = await Users.findByEmail(mail);

    if (user) {
      res.sendStatus(200); // Email found
    } else {
      res.status(404).json({ error: "Email not found" });
    }
  } catch (error) {
    console.error('Error verifying email:', error);
    res.status(500).json({ error: "Internal server error" });
  }
};
