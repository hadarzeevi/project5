// middleware/authenticateToken.js
const jwt = require('jsonwebtoken');
const secretKey = 'secretkeyHHE'; 

function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: "Access denied, token missing" });
  }

  jwt.verify(token, secretKey, (err, user) => {
    if (err) return res.status(403).json({ error: "Invalid token" });

    req.user = user;
    next();
  });
}

module.exports = authenticateToken;
// This middleware checks if the request has a valid JWT token in the Authorization header.