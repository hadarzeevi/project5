import React, { useState, useEffect } from 'react';
import { 
  FiMail, 
  FiArchive, 
  FiTrash2, 
  FiMoreHorizontal, 
  FiAlertTriangle, 
  FiPaperclip, 
  FiDownload, 
  FiEye, 
  FiArrowLeft 
} from 'react-icons/fi';
import ComposeModal from './ComposeModal';
import '../styles/MailView.css';

function MailView({ mail, darkMode, restoreMail, moveToSpam, onClose, handlePermanentDelete, selectedCategory,
   onUpdateMail, setSelectedMail, setSelectedCategory, removeFromSpam }) {
  const [user, setUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(false);
  const [errorUser, setErrorUser] = useState(null);
  const [showReplyModal, setShowReplyModal] = useState(false);
  const [showForwardModal, setShowForwardModal] = useState(false);
  const [showMenu, setShowMenu] = useState(false);
  const [showComposeModal, setShowComposeModal] = useState(false);
  const [draftToDeleteAfterSend, setDraftToDeleteAfterSend] = useState(null);


useEffect(() => {
  console.log("useEffect 1");
  if (!mail || !mail.from) {
    console.log("if (!mail || !mail.from) {");
    //setUser(null);
    return;
  }
  // check if mail exists and has a valid from field
  const fromId = typeof mail.from === 'string' ? mail.from : mail.from?._id;
  console.log("fromId:", fromId);

if (!fromId) {
  console.log("if (!fromId) {");
  //setUser(null);
  return;
}

    async function fetchUser() {
      console.log("async function fetchUser() {");
      setLoadingUser(true);
      setErrorUser(null);
      try {
        const res = await fetch(`http://localhost:4000/api/users/${fromId}`, {
          method: 'GET',
          headers: {
            Authorization: localStorage.getItem('token'),
          },
        });

        if (!res.ok) {
          const errorText = await res.text();
          throw new Error(`Failed to fetch user: ${res.status} - ${errorText}`);
        }
        console.log("res:", res);

        const data = await res.json();
        setUser(data);
        console.log("Fetched user:", data);
      } catch (err) {
        console.error('Error fetching user:', err);
        setErrorUser(err.message);
        setUser(null);
      } finally {
        setLoadingUser(false);
      }
    }

    fetchUser();
    console.log("Mail changed in MailView:", mail);
    console.log("mail.isRead3:", mail.isRead);
}, [mail]);

  useEffect(() => {
  console.log("useEffect 2");
  if (!mail || !(mail.id || mail._id) || mail.isRead) {
    console.log("if (!mail || !(mail.id || mail._id) || mail.isRead) {2");
     return;
  }

  const mailId = mail.id || mail._id;

  async function markMailAsRead() {
    console.log("async function markMailAsRead() {");
    try {
      const token = localStorage.getItem('token');
      if (!token) return;

      const res = await fetch(`http://localhost:4000/api/mails/${mailId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ isRead: true }),
      });
      console.log("mail.isRead:", mail.isRead);

      if (res.ok) {
        console.log("if (res.ok) {");
        console.log("mail.isRead2:", mail.isRead);
        const updatedMail = { ...mail, isRead: true };
        onUpdateMail?.(updatedMail); // עדכון המייל ב־state
        
      } else {
        const errorText = await res.text();
        console.error(`Failed to mark mail as read: ${res.status} - ${errorText}`);
      }
    } catch (err) {
      console.error("Network or fetch error:", err);
    }
  }

  markMailAsRead();
  console.log("markMailAsRead();");
  console.log("mail.isRead4:", mail.isRead);
}, [mail?.id, mail?._id, mail?.isRead]);


  const handleResend = () => {
    console.log("const handleResend = () => {");
    setDraftToDeleteAfterSend(mail);
    setShowComposeModal(true);
  };

  async function handleMarkAsUnread(id) {
    console.log("async function handleMarkAsUnread(id) {");
    try {
      const token = localStorage.getItem('token');
      if (!token) return;

      const res = await fetch(`http://localhost:4000/api/mails/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ isRead: false }),
      });

      if (res.ok) {
        const responseText = await res.text();
        let updatedMail;
        if (responseText.trim()) {
          try {
            updatedMail = JSON.parse(responseText);
          } catch {
            updatedMail = { ...mail, isRead: false };
          }
        } else {
          updatedMail = { ...mail, isRead: false };
        }
        setShowMenu(false);
        if (onUpdateMail) onUpdateMail(updatedMail);
        setSelectedMail(null);
        if (updatedMail.category && updatedMail.category !== selectedCategory) {
          setSelectedCategory(updatedMail.category);
        }
      } else {
        const errorText = await res.text();
        console.error(`Failed to mark as unread: ${res.status} - ${errorText}`);
        alert('Failed to mark as unread');
      }
    } catch (error) {
      console.error('Network error when marking as unread:', error);
      alert('Network error when marking as unread');
    }
  }

  const handleDownloadAttachment = (attachment) => {
    console.log("const handleDownloadAttachment = (attachment) => {");
    try {
      if (attachment.data) {
        const byteCharacters = atob(attachment.data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: attachment.contentType || 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = attachment.filename || 'attachment';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } else if (attachment.path) {
        const link = document.createElement('a');
        link.href = `http://localhost:4000/uploads/${attachment.filename}`;
        link.download = attachment.filename || 'attachment';
        link.click();
      }
    } catch (error) {
      console.error('Error downloading attachment:', error);
      alert('שגיאה בהורדת הקובץ');
    }
  };

  const handlePreviewAttachment = (attachment) => {
    console.log("const handlePreviewAttachment = (attachment) => {");
    try {
      if (attachment.data) {
        const byteCharacters = atob(attachment.data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: attachment.contentType || 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      } else if (attachment.path) {
        window.open(`http://localhost:4000/uploads/${attachment.filename}`, '_blank');
      }
    } catch (error) {
      console.error('Error previewing attachment:', error);
      alert('שגיאה בתצוגה מקדימה של הקובץ');
    }
  };

  const getFileIcon = (filename, contentType) => {
    console.log("const getFileIcon = (filename, contentType) => {");
    const extension = filename?.split('.').pop()?.toLowerCase();
    const type = contentType?.toLowerCase();

    if (type?.includes('image') || ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg'].includes(extension)) {
      return '🖼️';
    }
    if (type?.includes('pdf') || extension === 'pdf') {
      return '📄';
    }
    if (type?.includes('text') || ['txt', 'doc', 'docx'].includes(extension)) {
      return '📝';
    }
    if (['xlsx', 'xls', 'csv'].includes(extension)) {
      return '📊';
    }
    if (['zip', 'rar', '7z'].includes(extension)) {
      return '🗜️';
    }
    return '📎';
  };

  if (!mail) {
    return (
      <div className={`mailview-empty ${darkMode ? 'dark' : 'light'}`}>
        <div className="empty-content">
          <FiMail size={48} className="empty-icon" />
          <p className="empty-title">Select an email to view</p>
          <p className="empty-subtitle">Choose an email from the list to see the full content</p>
        </div>
      </div>
    );
  }

const fromName = user
  ? `${user.first_name} ${user.last_name}`
  : loadingUser
    ? 'Loading...'
    : errorUser
      ? 'Unknown'
      : 'Unknown';

console.log("fromName:", fromName);

  return (
    <div className="mail-view">
      <div className="mailview-header">
        <div className="mailview-header-left">
          <div className="back-and-subject">
            <button className="back-arrow" onClick={onClose}>
              <FiArrowLeft size={20} />
            </button>
            <h1 className="mailview-subject">{mail.subject}</h1>

            <div className="dropdown-wrapper">
              <button className="icon-btn" onClick={() => setShowMenu(prev => !prev)}>
                <FiMoreHorizontal size={18} />
              </button>

              {showMenu && (
                <div className={`dropdown-menu ${darkMode ? 'dark' : 'light'}`}>
                  {mail.category !== 'spam' && (
                    <button onClick={() => {
                      moveToSpam(mail);
                      setShowMenu(false);
                      setSelectedMail(null);
                    }}>Mark as Spam</button>
                  )}
                  {mail.category === 'spam' && (
                    <button onClick={() => {
                      removeFromSpam(mail);
                      setShowMenu(false);
                      setSelectedMail(null);
                    }}>Remove from Spam</button>
                  )}
                  <button onClick={() => handleMarkAsUnread(mail.id)}>Mark as Unread</button>
                  {selectedCategory === 'trash' && (
                    <button onClick={() => restoreMail(mail.id, mail.previousCategory)}>Restore</button>
                  )}
                  {selectedCategory === 'drafts' && (
                    <>
                      <button onClick={() => {handleResend(); setShowMenu(false);}}>Resend</button>
                      <button 
                        onClick={() => {
                          handlePermanentDelete(mail);
                          setShowMenu(false);
                        }}
                        className="delete-draft-btn"
                      >
                        Delete Draft
                      </button>
                    </>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="sender-info">
          <div className="sender-left">
            <div className="sender-avatar">
              {user?.profileImage ? (
              <img
                src={`http://localhost:4000/${user.profileImage.replace(/\\/g, '/')}`}
                alt="avatar"
              />
            ) : (
              fromName?.charAt(0).toUpperCase() || '?'
            )}

            </div>
            <div className="sender-details">
              <div className="sender-name">{fromName}</div>
              <div className="send-time">
                {new Date(mail.timestamp).toLocaleDateString()} at {new Date(mail.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </div>
              <div className="sender-email">{user?.mail ? user.mail : ''}</div>
            </div>
          </div>
        </div>
      </div>

      {mail.spam && (
        <div className="spam-warning">
          <FiAlertTriangle size={18} className="spam-icon" />
          <span>This email has been marked as spam due to suspicious content.</span>
        </div>
      )}

      <div className="mailview-body">
        <p className="mailview-text">{mail.body}</p>

        {mail.attachments && mail.attachments.length > 0 && (
          <div className="attachments-section">
            <div className="attachments-header">
              <FiPaperclip size={16} />
              <span>{mail.attachments.length} attachment{mail.attachments.length > 1 ? 's' : ''}</span>
            </div>
            <div className="attachments-list">
              {mail.attachments.map((attachment, index) => (
                <div key={index} className="attachment-item">
                  <div className="attachment-info">
                    <span className="attachment-icon">
                      {getFileIcon(attachment.filename, attachment.contentType)}
                    </span>
                    <div className="attachment-details">
                      <span className="attachment-name">{attachment.filename || `attachment-${index + 1}`}</span>
                      <span className="attachment-size">
                        {attachment.size ? `${(attachment.size / 1024).toFixed(1)} KB` : 'Unknown size'}
                      </span>
                    </div>
                  </div>
                  <div className="attachment-actions">
                    <button
                      className="attachment-btn preview-btn"
                      onClick={() => handlePreviewAttachment(attachment)}
                      title="תצוגה מקדימה"
                    >
                      <FiEye size={24} />
                    </button>
                    <button
                      className="attachment-btn download-btn"
                      onClick={() => handleDownloadAttachment(attachment)}
                      title="הורד קובץ"
                    >
                      <FiDownload size={24} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="mailview-footer">
        <div className="footer-buttons">
          <button className="btn primary" onClick={() => setShowReplyModal(true)}>Reply</button>
          <button className="btn secondary" onClick={() => setShowForwardModal(true)}>Forward</button>
        </div>
      </div>

      {showReplyModal && user && (
        <ComposeModal
          onClose={() => setShowReplyModal(false)}
          darkMode={darkMode}
          onMailSent={() => setShowReplyModal(false)}
          prefillTo={user.mail}
          prefillSubject={`Re: ${mail.subject}`}
          prefillBody={`\n\n--- Original message from ${user.mail} ---\n${mail.body}`}
        />
      )}

      {showForwardModal && (
        <ComposeModal
          onClose={() => setShowForwardModal(false)}
          darkMode={darkMode}
          onMailSent={() => setShowForwardModal(false)}
          prefillTo=""
          prefillSubject={`Fwd: ${mail.subject}`}
          prefillBody={`\n\n--- Forwarded message ---\nFrom: ${user?.mail || 'unknown'}\nDate: ${new Date(mail.timestamp).toLocaleString()}\nSubject: ${mail.subject}\n\n${mail.body}`}
          forwardAttachments={mail.attachments}
        />
      )}

      {showComposeModal && (
        <ComposeModal
          onClose={() => {
            setShowComposeModal(false);
            setDraftToDeleteAfterSend(null);
          }}
          darkMode={darkMode}
          onMailSent={() => {
            if (draftToDeleteAfterSend) {
              handlePermanentDelete(draftToDeleteAfterSend);
            }
            setShowComposeModal(false);
            setDraftToDeleteAfterSend(null);
            onClose();
          }}
          prefillTo={mail.toMail || ''}
          prefillSubject={mail.subject || ''}
          prefillBody={mail.body || ''}
          forwardAttachments={mail.attachments || []}
        />
      )}
    </div>
  );
}

export default MailView;
