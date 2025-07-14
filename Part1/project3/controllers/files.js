const path = require('path');
const fs   = require('fs');
const jwt  = require('jsonwebtoken');
const multer = require('multer');
const mongoose = require('mongoose');

const Mail = require('../models/mails');   // מודל המיילים
const User = require('../models/users');   // מודל המשתמשים

/* ───── Multer config ───── */
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, '..', 'uploads');
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    cb(null, dir);
  },
  filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});

const upload = multer({
  storage,
  limits: { fileSize: 10 * 1024 * 1024 } // 10 MB
});

/* ───── Send Mail ───── */
const sendMail = async (req, res) => {
  try {
    const { sendTo, subject, body } = req.body;
    const attachments = req.files ?? [];

    const token = req.headers.authorization?.split(' ')[1];
    if (!token) return res.status(401).json({ error: 'No token provided' });

    let decoded;
    try {
      decoded = jwt.verify(token, process.env.JWT_SECRET || 'secretkeyHHE');
    } catch {
      return res.status(401).json({ error: 'Invalid token' });
    }

    const sender    = await User.findById(decoded.userId).lean();
    const recipient = await User.findOne({ mail: sendTo }).lean();

    if (!sender)    return res.status(404).json({ error: 'Sender not found' });
    if (!recipient) return res.status(404).json({ error: 'Recipient not found' });

    if (!subject || !body)
      return res.status(400).json({ error: 'Missing subject or body' });

 const formattedAttachments = attachments.map(f => ({
  filename: f.filename,
  originalName: f.originalname,
  size: f.size,
  contentType: f.mimetype, 
  path: f.path
}));


    const base = {
      from      : sender._id,
      fromMail  : sender.mail,
      to        : recipient._id,
      toMail    : recipient.mail,
      subject,
      body,
      preview   : body.slice(0, 50) + '…',
      isRead    : false,
      spam      : false,
      timestamp : new Date(),
      attachments: formattedAttachments  // 🔧 משתמש בפורמט המתוקן
    };

    const inboxMail = new Mail({ ...base, ownerId: recipient._id, category: 'inbox' });
    const sentMail  = new Mail({ ...base, ownerId: sender._id,    category: 'sent'  });

    await Mail.insertMany([inboxMail, sentMail]);

    res.status(201).json({
      success : true,
      message : 'Email sent',
      sentId  : sentMail._id,
      attachments: attachments.length
    });
   
  } catch (err) {
    console.error('sendMail error:', err);
    res.status(500).json({ error: 'Internal server error' });
  }
};

const downloadAttachment = (req, res) => {
  try {
    const filename = req.params.filename;
    const filepath = path.join(__dirname, '..', 'uploads', filename);
    if (!fs.existsSync(filepath))
      return res.status(404).json({ error: 'File not found' });
    res.download(filepath);
  } catch (err) {
    console.error('downloadAttachment error:', err);
    res.status(500).json({ error: 'Failed to download file' });
  }
};

module.exports = { upload, sendMail, downloadAttachment };