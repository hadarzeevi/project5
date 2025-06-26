const Email = require('../models/mails');
const User = require('../models/users');

// Get the 50 most recent emails related to the user with pagination
exports.get50Emails = async (req, res) => {
  try {
    const userId = req.user.userId;
    const page = parseInt(req.query.page) || 1;
    const pageSize = 50;

    const skip = (page - 1) * pageSize;

    const allRelevantEmails = await Email.find({
      $or: [
        { from: userId },
        { to: userId }
      ]
    })
    .sort({ timestamp: -1 })
    .skip(skip)
    .limit(pageSize)
    .populate('from', 'mail username')
    .populate('to', 'mail username');

    const totalEmails = await Email.countDocuments({
      $or: [
        { from: userId },
        { to: userId }
      ]
    });

    const totalPages = Math.ceil(totalEmails / pageSize);

    res.json({
      mails: allRelevantEmails,
      totalPages,
      currentPage: page,
    });
  } catch (error) {
    console.error('Error fetching emails:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Get a single email by ID
exports.getMail = async (req, res) => {
  try {
    const id = req.params.id;
    const mail = await Email.findById(id)
      .populate('from', 'mail username')
      .populate('to', 'mail username');

    if (!mail) {
      return res.status(404).json({ error: "Mail not found" });
    }

    if (!mail.from._id.equals(req.user.userId) && !mail.to._id.equals(req.user.userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    res.status(200).json(mail);
  } catch (error) {
    console.error('Error fetching mail:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Update certain fields in a specific email
exports.patchMail = async (req, res) => {
  try {
    const userId = req.user.userId;
    const id = req.params.id;

    const mail = await Email.findById(id);

    if (!mail) {
      return res.status(404).json({ error: "Mail not found" });
    }

    if (!mail.from.equals(userId) && !mail.to.equals(userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    const allowedFields = ['subject', 'body', 'isRead', 'link', 'category', 'isStarred', 'previousCategory', 'label'];
    const updateData = {};

    for (const field of allowedFields) {
      if (req.body[field] !== undefined) {
        updateData[field] = req.body[field];
      }
    }

    await Email.findByIdAndUpdate(id, updateData, { new: true });

    res.status(200).json({ message: 'Mail updated successfully' });
  } catch (error) {
    console.error('Error updating mail:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Delete a specific email
exports.deleteMail = async (req, res) => {
  try {
    const userId = req.user.userId;
    const id = req.params.id;

    const mail = await Email.findById(id);

    if (!mail) {
      return res.status(404).json({ error: "Mail not found" });
    }

    if (!mail.from.equals(userId) && !mail.to.equals(userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await Email.findByIdAndDelete(id);

    res.status(204).json();
  } catch (error) {
    console.error('Error deleting mail:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Send a new email
exports.postMail = async (req, res) => {
  try {
    const userId = req.user.userId;
    const user = await User.findById(userId);
    const { sendTo, subject, body, isSpam, category } = req.body;

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!subject || !body) {
      return res.status(400).json({ error: "Missing subject or body" });
    }

    let recipient = null;
    if (sendTo) {
      recipient = await User.findOne({ mail: sendTo });
    }

    const preview = body.substring(0, 20) + '...';

    // אם זו טיוטה
    if (category === 'drafts') {
      const draftMail = new Email({
        ownerId: userId,
        from: userId,
        fromMail: user.mail,
        to: recipient ? recipient._id : null,
        toMail: recipient ? recipient.mail : '',
        subject,
        body,
        preview,
        isRead: true,
        category: 'drafts',
        previousCategory: 'drafts',
        isStarred: false,
      });

      const savedDraft = await draftMail.save();
      return res.status(201).json(savedDraft);
    }

    // אם זו שליחה רגילה
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(sendTo)) {
      return res.status(400).json({ error: "Invalid email format" });
    }

    if (!recipient) {
      return res.status(404).json({ error: 'Recipient user not found' });
    }

    // יצירת המייל הנשלח
    const sentMail = new Email({
      ownerId: userId,
      from: userId,
      fromMail: user.mail,
      to: recipient._id,
      toMail: recipient.mail,
      subject,
      body,
      preview,
      isRead: true,
      category: 'sent',
      previousCategory: 'sent',
      isStarred: false,
      label: [],
    });

    // יצירת המייל שמגיע לתיבת הדואר נכנס
    const inboxMail = new Email({
      ownerId: recipient._id,
      from: userId,
      fromMail: user.mail,
      to: recipient._id,
      toMail: recipient.mail,
      subject,
      body,
      preview,
      isRead: false,
      category: category || (isSpam ? 'spam' : 'inbox'),
      previousCategory: 'inbox',
      isStarred: false,
      label: [],
    });

    await sentMail.save();
    await inboxMail.save();

    return res.status(201).json(sentMail);
  } catch (error) {
    console.error("Error in postMail:", error);
    return res.status(500).json({ error: 'Internal server error' });
  }
};

// Search emails by query string
exports.searchEmails = async (req, res) => {
  try {
    const userId = req.user.userId;
    const query = req.params.query;

    if (!query) {
      return res.status(400).json({ error: 'Query parameter is required' });
    }

    const results = await Email.find({
      $and: [
        {
          $or: [
            { from: userId },
            { to: userId }
          ]
        },
        {
          $or: [
            { subject: { $regex: query, $options: 'i' } },
            { body: { $regex: query, $options: 'i' } },
            { fromMail: { $regex: query, $options: 'i' } }
          ]
        }
      ]
    })
    .populate('from', 'mail username')
    .populate('to', 'mail username')
    .sort({ timestamp: -1 });

    res.status(200).json(results);
  } catch (error) {
    console.error('Error searching emails:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Toggle label on mail
exports.toggleLabelOnMail = async (req, res) => {
  try {
    const userId = req.user.userId;
    const mailId = req.params.id;
    const labelId = req.body.labelId;
    const shouldAdd = req.body.shouldAdd;

    const mail = await Email.findById(mailId);
    if (!mail) {
      return res.status(404).json({ error: "Mail not found" });
    }

    if (!mail.ownerId.equals(userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    if (shouldAdd) {
      if (!mail.labelId.includes(labelId)) {
        mail.labelId.push(labelId);
      }
    } else {
      mail.labelId = mail.labelId.filter(id => !id.equals(labelId));
    }

    await mail.save();
    res.status(200).json({ message: `Label ${shouldAdd ? 'added' : 'removed'} successfully` });
  } catch (error) {
    console.error('Error toggling label:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};