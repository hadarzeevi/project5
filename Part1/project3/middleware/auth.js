// middleware/authenticateToken.js
const jwt = require('jsonwebtoken');
const mongoose = require('mongoose'); 
const secretKey = 'secretkeyHHE'; 

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: "Access denied, token missing" });
  }

  jwt.verify(token, secretKey, (err, user) => {
    if (err) return res.status(403).json({ error: "Invalid token" });

  
    if (user.userId && mongoose.Types.ObjectId.isValid(user.userId)) {
      user.userId = new mongoose.Types.ObjectId(user.userId);
    }

    req.user = user;
    next();
  });
}

module.exports = authenticateToken;
