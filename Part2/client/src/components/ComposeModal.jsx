import { useState, useEffect } from 'react';
import AttachmentsList from './AttachmentsList';
import useFileUpload from '../hooks/useFileUpload';
import '../styles/ComposeModal.css';

function ComposeModal({ 
  onClose, 
  darkMode, 
  onMailSent, 
  prefillTo = '', 
  prefillSubject = '', 
  prefillBody = '',
  forwardAttachments = [] // attachments from the forwarded email
}) {
  const [to, setTo] = useState(prefillTo);
  const [subject, setSubject] = useState(prefillSubject);
  const [body, setBody] = useState(prefillBody);
  const [mails, setMails] = useState([]);
  const [sending, setSending] = useState(false);

  // using custom hook for file uploads
  const { 
    attachments, 
    handleFileUpload, 
    removeAttachment, 
    clearAttachments,
    addAttachmentsFromForward
  } = useFileUpload(10); // maximum size 10MB

  // adding forwarded attachments to the attachments list
  useEffect(() => {
    if (forwardAttachments && forwardAttachments.length > 0) {
      if (addAttachmentsFromForward) {
        addAttachmentsFromForward(forwardAttachments);
      }
    }
  }, [forwardAttachments, addAttachmentsFromForward]);

const handleSend = async () => {
    if (!to.trim() || !subject.trim()) {
      alert('Please fill in recipient and subject');
      return;
    }

    setSending(true);
    
  try {
    const token = localStorage.getItem("token");

    // check if the email is spam using the blacklist API
    const spamResponse = await fetch('http://localhost:4000/api/blacklist/check', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ subject, body })
    });

    const spamResult = await spamResponse.json();
    const isSpam = spamResult.spam;

    // send the email even if it's marked as spam
      
      if (attachments.length > 0 || forwardAttachments.length > 0) {
        // create FormData for file uploads
        const formData = new FormData();
        formData.append('sendTo', to);
        formData.append('subject', subject);
        formData.append('body', body);
        formData.append('isSpam', isSpam);
        // adding attachments to FormData
        attachments.forEach((file) => {
          formData.append('attachments', file);
        });

        if (forwardAttachments && forwardAttachments.length > 0) {
          forwardAttachments.forEach((attachment) => {
            // Convert base64 data to Blob/File if necessary
            if (attachment.data) {
              try {
                const byteCharacters = atob(attachment.data);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                  byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], { type: attachment.contentType || 'application/octet-stream' });
                const file = new File([blob], attachment.filename, { type: attachment.contentType });
                formData.append('attachments', file);
              } catch (error) {
                console.error('Error converting attachment:', error);
              }
            }
          });
        }

        const response = await fetch('http://localhost:4000/api/files/send', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
          body: formData
        });

        if (response.ok) {
          const result = await response.json();
          const totalAttachments = attachments.length + (forwardAttachments?.length || 0);
          alert("Email sent successfully with " + totalAttachments + " attachments!");
          clearAttachments();
          onClose();
          if (onMailSent) onMailSent();
        } else {
          const error = await response.json();
          alert("Error: " + (error.message || error.error));
        }
      } else {
        // send email without attachments
      const response = await fetch('http://localhost:4000/api/mails', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
      },
        body: JSON.stringify({
            sendTo: to,
            subject: subject,
            body: body,
            isSpam
         })
      });

        if (response.ok) {
          alert("Email sent successfully!");
          onClose();
          if (onMailSent) onMailSent();
        } else {
          const error = await response.json();
          alert("Error: " + (error.error || error.message));
        }
      }
    } catch (err) {
      console.error("Send error:", err);
      alert("An error occurred: " + err.message);
    } finally {
      setSending(false);
    }
  };

// function to move the email to drafts - the function is called when the user clicks "Cancel"
const moveToDraft = async (mailData) => {
  try {
    const token = localStorage.getItem("token");

    const response = await fetch('http://localhost:4000/api/mails', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify({
        sendTo: mailData.to || '',
        subject: mailData.subject || '',
        body: mailData.body || '',
        category: 'drafts',
      }),
    });

    if (response.ok) {
      const newDraft = await response.json();
      
      if (onMailSent) {
        onMailSent(newDraft);
      }
      
      alert("Email saved as draft");
    } else {
      const err = await response.json();
      console.error("Server error:", err);
      alert("Failed to save draft: " + (err.message || err.error));
    }
  } catch (error) {
    console.error("Network error:", error);
    alert("Error saving draft: " + error.message);
  }
};

  // get the modal title based on the prefillSubject
  const getModalTitle = () => {
    if (prefillSubject.startsWith('Re:')) return 'Reply';
    if (prefillSubject.startsWith('Fwd:')) return 'Forward';
    return 'New Message';
  };

  return (
    <div className="compose-overlay">
      <div className={`compose-modal ${darkMode ? 'dark' : 'light'}`}>
        <div className="compose-header">
          <div className="compose-header-left">
            <h2 className="compose-title">{getModalTitle()}</h2>
          </div>
          <div className="compose-header-right">
            <button onClick={onClose} className="icon-button close-button">
              <i className="bi bi-x" style={{ fontSize: '20px' }}></i>
            </button>
          </div>
        </div>

        <div className="compose-body">
          <input
            type="email"
            placeholder="To..."
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="compose-input"
          />
          <input
            type="text"
            placeholder="Subject"
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            className="compose-input"
          />

          {/* componenets AttachmentsList*/}
          <AttachmentsList 
            attachments={attachments}
            onRemoveAttachment={removeAttachment}
            darkMode={darkMode}
            forwardedAttachments={forwardAttachments}
          />

          <textarea
            placeholder="Write your email here..."
            value={body}
            onChange={(e) => setBody(e.target.value)}
            className="compose-textarea"
          />
        </div>

        <div className="compose-footer">
          {/* button to upload file */}
          <label className="icon-button attach-button">
            <i className="bi bi-paperclip" style={{ fontSize: '20px' }}></i>            <input
              type="file"
              multiple
              onChange={handleFileUpload}
              style={{ display: 'none' }}
              accept="*/*"
            />
          </label>
          
          <div className="footer-buttons">
            <button onClick={() => {
              moveToDraft({to, subject, body});              
              onClose(); }}
              className="cancel-button">
              Cancel
            </button>
            <button 
              onClick={handleSend} 
              className="send-button"
              disabled={sending}
            >
              <i className="bi bi-send" style={{ fontSize: '16px' }}></i>
              {sending ? 'Sending...' : 'Send'}
              {(attachments.length > 0 || forwardAttachments.length > 0) && 
                ` (${attachments.length + (forwardAttachments?.length || 0)} files)`}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ComposeModal;