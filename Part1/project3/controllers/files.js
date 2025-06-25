const multer = require('multer');
const path = require('path');
const fs = require('fs');
const jwt = require('jsonwebtoken');
const Emails = require('../models/mails');
const Users = require('../models/users');

    
// This code handles file uploads and downloads in the email system using multer.
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadPath = path.join(__dirname, '..', 'uploads');
    // create the uploads directory if it doesn't exist
    if (!fs.existsSync(uploadPath)) {
      fs.mkdirSync(uploadPath, { recursive: true });
    }
    cb(null, uploadPath);
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + '-' + file.originalname);
  }
});

const upload = multer({ 
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB limit
  }
});

const sendMail = async (req, res) => {
  try {
    
    const { sendTo, subject, body } = req.body; 
    const attachments = req.files || [];

    // validate JWT token
    const token = req.headers.authorization && req.headers.authorization.split(' ')[1];
    if (!token) {
      return res.status(401).json({ success: false, message: 'No token provided' });
    }

    let decoded;
    try {
      decoded = jwt.verify(token, 'secretkeyHHE');
    } catch (err) {
      return res.status(401).json({ success: false, message: 'Invalid token' });
    }

    const userId = decoded.userId;
    const user = Users.getUser(userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    // check if recipient exists
    const recipient = sendTo ? Users.getUserByMail(sendTo) : null;
    if (!recipient) {
      return res.status(404).json({ success: false, message: 'Recipient not found' });
    }
    // validate required fields
    if (!sendTo || !subject || !body) {
      return res.status(400).json({ 
        success: false, 
        message: 'Missing required fields: sendTo, subject, or body' 
      });
    }

    // validate email format like mail.js
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(sendTo)) {
      return res.status(400).json({ success: false, message: "Invalid email format" });
    }

    const mailData = {
      ownerId: userId,
      from: userId,
      fromMail: user.mail,
      to: parseInt(sendTo),
      toMail: recipient.mail,
      subject,
      body,
      timestamp: new Date().toISOString(),
      spam: false,
      preview: body.substring(0, 50) + '...',
      isRead: false,
      category: 'sent',
      attachments: attachments.map(file => ({
        originalName: file.originalname,
        filename: file.filename,
        path: file.path,
        size: file.size,
        mimetype: file.mimetype
      }))
    };

    const inboxMail = {
      ...mailData,
      category: 'inbox',
      ownerId: parseInt(sendTo)
    };
    

    // update IDs for mailData and inboxMail
    mailData.to = recipient.id;
    mailData.id = Emails.emails.length + 1;
    
    inboxMail.to = recipient.id;
    inboxMail.ownerId = recipient.id;
    inboxMail.id = Emails.emails.length + 2;

    // saving mails like in mails.js
    Emails.addMail(mailData);
    Emails.addMail(inboxMail);
    // save attachments to the database
    res.json({ 
      success: true, 
      message: 'Email sent successfully',
      attachments: attachments.length
    });

  } catch (error) {
    console.error('Error sending email:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};


const downloadAttachment = (req, res) => {
  try {
    const filename = req.params.filename;
    const filepath = path.join(__dirname, '..', 'uploads', filename); 

    if (!fs.existsSync(filepath)) {
      return res.status(404).json({ error: 'File not found' });
    }

    res.download(filepath);
  } catch (error) {
    console.error('Error downloading file:', error);
    res.status(500).json({ error: 'Failed to download file' });
  }
};

module.exports = {
  upload,
  sendMail,
  downloadAttachment
};