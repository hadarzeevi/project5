const Emails = require('../models/mails');
const Users = require('../models/users');

// Get the 50 most recent emails related to the user
// exports.get50Emails = (req, res) => {
//   const userId = req.user.userId; 
//   const relaventEmails = Emails.emails
//     .filter(e => e.from === userId || e.to === userId)
//     .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
//     .slice(0, 50);

//     res.json(relaventEmails);
// };
exports.get50Emails = (req, res) => {
  const userId = req.user.userId;
  const page = parseInt(req.query.page) || 1;
  const pageSize = 50;

  const allRelevantEmails = Emails.emails
    .filter(e => e.from === userId || e.to === userId)
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

  const totalEmails = allRelevantEmails.length;
  const totalPages = Math.ceil(totalEmails / pageSize);

  // חיתוך לפי עמוד
  const startIndex = (page - 1) * pageSize;
  const paginatedEmails = allRelevantEmails.slice(startIndex, startIndex + pageSize);

  res.json({
    mails: paginatedEmails,
    totalPages,
    currentPage: page,
  });
};

// Get a single email by ID
exports.getMail = (req, res) => {
  const id = req.params.id;
  const mail = Emails.getMail(id);
  if (mail) {
    if (mail.from !== req.user.id && mail.to !== req.user.id) {
       return res.status(403).json({ error: "Access denied" });
  }
    res.status(200).json(mail);
  } else {
    res.status(404).json({ error: "Mail not found" });
  }
};

// Update certain fields in a specific email
exports.patchMail = (req, res) => {
  //header
  const userId = req.user.userId;
  const id = req.params.id;
  const mail = Emails.getMail(id); 

  if (!mail) {
    return res.status(404).json({ error: "Mail not found" });
  }
  if (mail.from !== userId && mail.to !== userId) {
  return res.status(403).json({ error: "Access denied" });
}

  // change body
  const allowedFields = ['subject', 'body', 'isRead', 'link', 'category', 'isStarred', 'previousCategory', 'label'];
  for (const field of allowedFields) {
    if (req.body[field] !== undefined) {
      mail[field] = req.body[field];
    }
  }
  //save changes
  Emails.patchMail(id, mail); 

  res.status(200).json();
};

// Delete a specific email
exports.deleteMail = (req, res) => {
  //header
  const userId = req.user.userId;
  const id = req.params.id;
  const mail = Emails.getMail(id); 

  if (!mail) {
    return res.status(404).json({ error: "Mail not found" });
  }
   if (mail.from !== userId && mail.to !== userId) {
      return res.status(403).json({ error: "Access denied" });
    }

  //save changes
  Emails.deleteMail(id); 

  res.status(204).json();
};
// Send a new email
exports.postMail = async (req, res) => {
  try {
    const userId = req.user.userId;
    const user = Users.getUser(userId);
    const { sendTo, subject, body, isSpam, category } = req.body;

    // בדיקות בסיסיות
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!subject || !body) {
      return res.status(400).json({ error: "Missing subject or body" });
    }

    // ננסה למצוא את המקבל אם נשלח
    const recipient = sendTo ? Users.getUserByMail(sendTo) : null;

    const preview = body.substring(0, 20) + '...';

    // אם זו טיוטה
    if (category === 'drafts') {
      const DraftMail = {
        id: Emails.emails.length + 1,
        ownerId: userId,
        from: userId,
        fromMail: user.mail,
        to: recipient ? recipient.id : null,
        toMail: recipient ? recipient.mail : '',
        subject,
        body,
        timestamp: new Date().toISOString(),
        preview,
        isRead: true,
        category: 'drafts',
        previousCategory: 'drafts',
        isStarred: false,
      };

      Emails.addMail(DraftMail);
      return res.status(201).json(DraftMail);
    }

    // אם זו שליחה רגילה — נוודא שכתובת תקינה
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(sendTo)) {
      return res.status(400).json({ error: "Invalid email format" });
    }

    if (!recipient) {
      return res.status(404).json({ error: 'Recipient user not found' });
    }

    const SentMail = {
      id: Emails.emails.length + 1,
      ownerId: userId,
      from: userId,
      fromMail: user.mail,
      to: recipient.id,
      toMail: recipient.mail,
      subject,
      body,
      timestamp: new Date().toISOString(),
      preview,
      isRead: true,
      category: 'sent',
      previousCategory: 'sent',
      isStarred: false,
      label: [],
    };

    const InboxMail = {
      id: Emails.emails.length + 2,
      ownerId: recipient.id,
      from: userId,
      fromMail: user.mail,
      to: recipient.id,
      toMail: recipient.mail,
      subject,
      body,
      timestamp: new Date().toISOString(),
      preview,
      isRead: false,
      category: category || (isSpam ? 'spam' : 'inbox'),
      previousCategory: 'inbox',
      isStarred: false,
      label: [],
    };

    Emails.addMail(SentMail);
    Emails.addMail(InboxMail);

    console.log(Emails.emails.map(m => ({
      id: m.id,
      ownerId: m.ownerId,
      category: m.category,
      subject: m.subject
    })));

    return res.status(201).json(SentMail); // מחזירים את המייל שנשלח
  } catch (err) {
    console.error("Error in postMail:", err);
    return res.status(500).json({ error: 'Internal server error' });
  }
};


// Search emails by query string
exports.searchEmails = (req, res) => {
  const userId = req.user.userId;
  const query = req.params.query;
  if (!query) {
    return res.status(400).json({ error: 'Query parameter is required' });
  }
  const results = Emails.search(query);
  res.status(200).json(results);
};

exports.toggleLabelOnMail = (req, res) => {
  const userId = req.user.userId;
  const mailId = parseInt(req.params.id);
  const labelId = parseInt(req.body.labelId);
  const shouldAdd = req.body.shouldAdd;

  const mail = Emails.getMail(mailId);
  if (!mail) {
    return res.status(404).json({ error: "Mail not found" });
  }

  if (mail.ownerId !== userId) {
    return res.status(403).json({ error: "Access denied" });
  }

  // ודא שיש לו את השדה labeldId
  if (!Array.isArray(mail.labelId)) {
    mail.labelId = [];
  }

  if (shouldAdd) {
    if (!mail.labelId.includes(labelId)) {
      mail.labelId.push(labelId);
    }
  } else {
    mail.labelId = mail.labelId.filter(id => id !== labelId);
  }

  Emails.patchMail(mailId, mail);
  res.status(200).json({ message: `Label ${shouldAdd ? 'added' : 'removed'} successfully` });
};