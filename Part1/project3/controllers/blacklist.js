const BlacklistService = require('../services/BlacklistServer');
const Emails = require('../models/mails');

exports.addToBlackList = async (req, res) => {
    const { subject = '', body = '' } = req.body;
    // extract URLs from subject and body
    const urls = [...BlacklistService.extractUrls(subject), ...BlacklistService.extractUrls(body)];

    if (urls.length === 0) {
        return res.status(400).json({ error: 'No URLs found in subject or body' });
    }

    try {
        let successCount = 0;
        for (let url of urls) {
            url = decodeURIComponent(url).trim();
            const response = await BlacklistService.add(url);

            if (response === '201 Created') {
                successCount++;
            }
        }

        if (successCount > 0) {
            res.status(201).json({ message: `${successCount} URLs added to blacklist` });
        } else {
            res.status(400).json({ error: 'No URLs were added to blacklist' });
        }

    } catch (error) {
        res.status(500).json({ error: 'Failed to add URLs to blacklist', details: error.message || error });
    }
};

exports.removeFromBlackList = async (req, res) => {
    const { id } = req.params;
    if (!id) {
        return res.status(400).json({ error: 'Mail ID is required' });
    }

    try {
        //const mail = Emails.getMail(id); 
        const mail = await Emails.findById(id); 
        if (!mail) {
            return res.status(404).json({ error: 'Mail not found' });
        }

        const subject = mail.subject || '';
        const body = mail.body || '';
        const urls = [...BlacklistService.extractUrls(subject), ...BlacklistService.extractUrls(body)];

        if (urls.length === 0) {
            return res.status(400).json({ error: 'No URLs found in mail content' });
        }

        let successCount = 0;
        for (let url of urls) {
            url = decodeURIComponent(url).trim();
            const response = await BlacklistService.remove(url);
            if (response === '204 No Content') {
                successCount++;
            }
        }

        if (successCount > 0) {
            res.status(200).json({ message: `${successCount} URLs removed from blacklist` });
        } else {
            res.status(404).json({ error: 'No URLs found in blacklist to remove' });
        }

    } catch (error) {
        res.status(500).json({ error: 'Failed to remove URLs from blacklist', details: error.message });
    }
};

exports.checkEmailForSpam = async (req, res) => {
    const { subject, body } = req.body;

    if (!subject || !body) {
        return res.status(400).json({ error: 'subject and body are required' });
    }

    try {
        const isSpam = await BlacklistService.check(subject, body);
        res.status(200).json({ spam: isSpam });
    } catch (err) {
        res.status(500).json({ error: 'Error checking spam', details: err.message });
    }
};
