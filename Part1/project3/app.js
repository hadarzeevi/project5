const express = require ('express');
const cors = require('cors');
const app = express();
const path = require('path');

app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true,
  methods: ['GET', 'POST','PATCH', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'user-id']
}));
app.use(express.json());

const usersU = require('./routes/users');
const usersT = require('./routes/tokens');
const mails = require('./routes/mails');
const blacklist = require('./routes/blacklist');
const labels = require('./routes/labels');
const filesRoutes = require('./routes/files'); 


app.use('/api/users', usersU);
app.use('/api/tokens', usersT);
app.use('/api/mails', mails);
//app.use('/api/emails', mails);
app.use('/api/labels', labels);
app.use('/api/blacklist', blacklist);
app.disable('etag');
// Static files for uploaded files
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use('/api/files', filesRoutes);


app.listen(4000, () => { 
console.log("Server running on port 4000");
});
