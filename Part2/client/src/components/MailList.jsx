import { useState, useEffect } from 'react';
import '../styles/MailList.css';
import LabelDropdown from './LabelDropdown';


// הכנתי פונקציה עזר למיפוי _id ל-id בשורת המיילים
function mapMailId(mail) {
  return {
    ...mail,
    id: mail._id ? mail._id.toString() : mail.id,
  };
}

function MailItemWithUser({ mail, selectedMail, onSelect, darkMode, 
  toggleStar, moveToTrash, onAddLabelToMail, onRemoveLabelFromMail }) {
  const [user, setUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(false);
  const [showLabelDropdown, setShowLabelDropdown] = useState(false);

  console.log("MailView mail:", mail);

  useEffect(() => {
    console.log("MailView useEffect activated. Mail ID:", mail?._id);
    console.log("MailView mail:", mail);
console.log("mail.from:", mail?.from);

    if (!mail || !mail.from) {
    setUser(null);
    return;
  }

    async function fetchUser() {
      setLoadingUser(true);
      try {
        const res = await fetch(`http://localhost:4000/api/users/${mail.from}`, {
          headers: {
            'user-id': mail.from,
          },
        });

        if (res.ok) {
          const data = await res.json();
          setUser(data);
        }
      } catch (err) {
        console.error('Failed to fetch user:', err);
      } finally {
        setLoadingUser(false);
      }
    }

    fetchUser();
  }, [mail?._id]);

  const isSelected = selectedMail?.id === mail.id;
  const isUnread = !mail.isRead;
  const hasAttachments = mail.attachments && mail.attachments.length > 0;
  
  const senderName = loadingUser 
    ? 'Loading...' 
    : user 
      ? `${user.first_name} ${user.last_name}` 
      : mail.from;

  return (
    <div
      key={mail.id}
      onClick={() => onSelect({ ...mail })}
      className={`mail-item 
        ${isSelected ? 'selected' : ''} 
        ${isUnread ? 'unread' : ''} 
        ${mail.spam ? 'spam' : ''}`}
    >
      <div className="mail-item-content">
        <div className="mail-item-meta">
          {mail.category !== 'trash' && (
            <button
              className="star-button"
              onClick={(e) => {
                e.stopPropagation();
                toggleStar(mail.id, mail.isStarred);
              }}
            >
              <i
                className={`bi ${mail.isStarred ? 'bi-star-fill' : 'bi-star'} star-icon`}
                style={{ fontSize: '16px' }}
              ></i>
            </button>
          )}
          {mail.category !== 'trash' && (
            <div className="relative">
              <button
                className="label-button"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowLabelDropdown(!showLabelDropdown);
                }}
                title="Manage labels"
              >
                <i className="bi bi-tag" style={{ fontSize: '16px' }}></i>
              </button>
              
              <LabelDropdown
                // יש להעביר props רלוונטיים לפי הצורך כאן
              />
            </div>
          )}
          {mail.category !== 'trash' && (
          <button
            className="trash-button"
            onClick={(e) => {
              e.stopPropagation();
              moveToTrash(mail.id);
            }}
          >
            <i className="bi bi-trash" style={{ fontSize: '20px' }}></i>
          </button>
          )}
          {mail.isImportant && <div className="important-dot"></div>}
          {hasAttachments && (
            <div className="attachment-indicator" title={`${mail.attachments.length} attachment${mail.attachments.length > 1 ? 's' : ''}`}>
              <i className="bi bi-paperclip" style={{ fontSize: '14px' }}></i>
            </div>
          )}
        </div>

        <div className="mail-details">
          <div className="mail-header">
            <span className={`mail-sender ${isUnread ? 'bold' : ''}`}>{senderName}</span>
            <div className="mail-header-right">
              <span className="mail-date">{mail.date}</span>
            </div>
          </div>

          <div className={`mail-subject ${isUnread ? 'bold' : ''}`}>
            {mail.subject}
            {hasAttachments && (
              <span className="subject-attachment-count">
                ({mail.attachments.length})
              </span>
            )}
          </div>
          <div className="mail-preview">{mail.preview}</div>
          {mail.labels && mail.labels.length > 0 && (
            <div className="mail-labels">
              {mail.labels.map((label, index) => (
                <span 
                  key={index}
                  className="label-tag"
                  style={{ 
                    backgroundColor: label.color || '#4285f4',
                    color: 'white',
                    fontSize: '11px',
                    padding: '2px 6px',
                    borderRadius: '10px',
                    marginRight: '4px',
                    display: 'inline-block'
                  }}
                >
                  {typeof label === 'string' ? label : label.name}
                </span>
              ))}
            </div>
          )}

          {mail.spam && (
            <div className="mail-category">
              <span className="category-label spam">Spam</span>
            </div>
          )}

          {mail.category && !mail.spam && (
            <div className="mail-category">
              <span className={`category-label ${mail.category.toLowerCase()}`}>{mail.category}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function MailList({ mails,
  selectedMail,
  onSelect,
  loading,
  error,
  darkMode,
  selectedCategory,
  onRefresh,
  toggleStar,
  moveToTrash,
  handlePermanentDelete,
  currentPage,
  totalPages,
  setCurrentPage,
  onAddLabelToMail,
  onRemoveLabelFromMail,
  onRemoveLabelFromMails,
  setMails,
  selectedLabel,
  searchTerm
  }) {

  // מפה את ה־mails עם id
  const mappedMails = mails.map(mapMailId);

  const [selectedMails, setSelectedMails] = useState([]);
  const [showBulkLabelDropdown, setShowBulkLabelDropdown] = useState(false);

  const toggleSelectMail = (mailId) => {
    setSelectedMails(prev =>
      prev.includes(mailId) ? prev.filter(id => id !== mailId) : [...prev, mailId]
    );
  };

  const clearSelection = () => setSelectedMails([]);
  const themeClass = darkMode ? 'dark' : 'light';
  
  if (loading) {
    return (
      <div className={`mail-list-wrapper center ${themeClass}`}>
        <div className="loading-message">
          <i className="bi bi-arrow-repeat" style={{ fontSize: '20px' }}></i>
          <span>Loading emails...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`mail-list-wrapper center ${themeClass}`}>
        <div className="error-message">
          <p>Error loading emails</p>
          <p className="error-detail">{error}</p>
        </div>
      </div>
    );
  }

  if (mappedMails.length === 0) {
    return (
      <div className={`mail-list-wrapper ${themeClass}`}>
        <div className="mail-list-header">
          <div className="header-left">
            <h2 className="header-title">{selectedCategory}</h2>
          </div>
          <div className="header-actions">
            <button
              className="icon-button"
              onClick={onRefresh}
              aria-label="רענן מיילים"
            >
              <i className="bi bi-arrow-repeat" style={{ fontSize: '20px' }}></i>
            </button>
          </div>
        </div>
        <div className="center empty-state">
          <i className="bi bi-envelope" style={{ fontSize: '20px' }}></i>
          <p className="empty-title">No emails in {selectedCategory.toLowerCase()}</p>
          <p className="empty-text">When you receive emails, they'll appear here</p>
        </div>
      </div>
    );
  }

  // הפונקציות togglelabel ו-deleteMilFromLabel תיקנתי את השם מ-label ל-labels ושימוש ב-id

  const togglelabel = async (mailId, labelname) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) throw new Error("No token found.");
      const mailToMove = mappedMails.find(mail => mail.id === mailId);
      const updatedLabels = mailToMove.labels ? [...mailToMove.labels, labelname] : [labelname];

      const res = await fetch(`http://localhost:4000/api/mails/${mailId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ labels: updatedLabels }),
      });

      if (!res.ok) {
        throw new Error("Failed to toggle label");
      }

      setMails((prevMails) =>
        prevMails.map((mail) =>
          (mail._id?.toString() === mailId || mail.id === mailId)
            ? { ...mail, labels: updatedLabels }
            : mail
        )
      );
    } catch (err) {
      console.error(err);
    }
  };

  const deleteMilFromLabel = async (mailId, labelname) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) throw new Error("No token found.");
      const mailToMove = mappedMails.find(mail => mail.id === mailId);
      const updatedLabels = mailToMove.labels.filter(l => l !== labelname);

      const res = await fetch(`http://localhost:4000/api/mails/${mailId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ labels: updatedLabels }),
      });

      if (!res.ok) {
        throw new Error("Failed to delete mail from label");
      }

      setMails((prevMails) =>
        prevMails.map((mail) =>
          (mail._id?.toString() === mailId || mail.id === mailId)
            ? { ...mail, labels: updatedLabels }
            : mail
        )
      );
    } catch (err) {
      console.error(err);
    }
  };

  // מסנן לפי קטגוריה ותווית
  const filteredMails = mappedMails.filter((mail) => {
    const category = selectedCategory?.toLowerCase();
    const label = selectedLabel;
    if (category === 'starred') {
      return mail.isStarred === true;
    }
    if (category) {
      return mail.category?.toLowerCase() === category;
    }
    if (label) {
      return mail.labels && mail.labels.includes(label);
    }
    return true;
  });

  function highlightSearch(text, searchTerm) {
    if (!searchTerm || !text) return text;
    const regex = new RegExp(`(${searchTerm})`, 'gi');
    return text.replace(regex, '<mark>$1</mark>');
  }

  return (
    <div className={`mail-list-wrapper ${themeClass}`}>
      <div className="mail-list-header">
        <div className="header-left">
          <h2 className="header-title">{selectedLabel || selectedCategory}</h2>
        </div>
        <div className="header-actions">
          <div className="bulk-actions-buttons">
            {selectedLabel ? (
              <button
                className="icon-button"
                onClick={() => {
                  selectedMails.forEach((id) => {
                    deleteMilFromLabel(id, selectedLabel);
                  });
                  clearSelection();
                }}
                aria-label={`הסר תווית "${selectedLabel}"`}
                title={`הסר תווית "${selectedLabel}"`}
              >
                <div style={{ position: 'relative', width: 18, height: 18 }}>
                  <i className="bi bi-tag" style={{ fontSize: '18px' }}></i>
                  <i
                    className="bi bi-slash"
                    style={{
                      position: 'absolute',
                      top: 0,
                      left: 0,
                      opacity: 0.8,
                      fontSize: '18px'
                    }}
                  ></i>
                </div>
              </button>
            ) : (
              <div className="relative">
                <button
                  className="icon-button"
                  onClick={() => setShowBulkLabelDropdown(prev => !prev)}
                  aria-label="ניהול תוויות"
                  title="ניהול תוויות"
                >
                  <i className="bi bi-tag" style={{ fontSize: '18px' }}></i>
                </button>
                <LabelDropdown
                  isOpen={showBulkLabelDropdown}
                  onClose={() => setShowBulkLabelDropdown(false)}
                  onAddToLabel={(labelname) => {
                    selectedMails.forEach((id) => togglelabel(id, labelname));
                    clearSelection();
                    alert("Label added successfully!"); 
                    setShowBulkLabelDropdown(false);
                  }}
                  onRemoveFromLabel={(labelName) => {
                    selectedMails.forEach((id) => deleteMilFromLabel(id, labelName));
                    clearSelection();
                  }}
                  currentLabels={[]}
                  position="bottom-left"
                  darkMode={darkMode}
                />
              </div>
            )}

            <button
              className="icon-button"
              onClick={async () => {
                try {
                  if (selectedCategory === 'trash' || selectedCategory === 'drafts') {
                    const mailsToDelete = selectedMails
                      .map((id) => mappedMails.find((m) => m.id === id))
                      .filter(Boolean);
                    await handlePermanentDelete(mailsToDelete);
                  } else {
                    selectedMails.forEach((id) => {
                      moveToTrash(id);
                    });
                    clearSelection();
                  }
                } catch (error) {
                  console.error('Error deleting mails:', error);
                }
              }}
              disabled={selectedMails.length === 0}
              aria-label={selectedCategory === 'trash' ? "מחק לצמיתות" : "העבר לאשפה"}
              title={selectedCategory === 'trash' ? "מחק לצמיתות" : "העבר לאשפה"}
            >
              <i className="bi bi-trash" style={{ fontSize: '18px' }}></i>
            </button>

            <button
              onClick={clearSelection}
              className="icon-button"
              aria-label="בטל בחירה"
              title="בטל בחירה"
            >
              <i className="bi bi-x-circle" style={{ fontSize: '18px' }}></i>
            </button>

            <button
              onClick={() => setSelectedMails(filteredMails.map(m => m.id))}
              className="icon-button"
              aria-label="בחר הכל"
              title="בחר הכל"
            >
              <i className="bi bi-circle" style={{ fontSize: '18px' }}></i>
            </button>
            
            <button
              className="icon-button"
              onClick={onRefresh}
              aria-label="רענן מיילים"
              title="רענן מיילים"
            >
              <i className="bi bi-arrow-repeat" style={{ fontSize: '18px' }}></i>
            </button>
          </div>
          <div className="pagination-controls">
            <button
              onClick={() => setCurrentPage(p => Math.max(p - 1, 1))}
              disabled={currentPage === 1}
              className="page-arrow"
              title="עמוד קודם"
            >
              <i className="bi bi-chevron-left" style={{ fontSize: '18px' }}></i>
            </button>

            <span className="page-indicator">
              {currentPage} / {totalPages}
            </span>

            <button
              onClick={() => setCurrentPage(p => Math.min(p + 1, totalPages))}
              disabled={currentPage === totalPages}
              className="page-arrow"
              title="עמוד הבא"
            >
              <i className="bi bi-chevron-right" style={{ fontSize: '18px' }}></i>
            </button>
          </div>
        </div>
      </div>

      <div className="mail-list-scroll">
        {filteredMails.map((mail) => {
          const isSelected = selectedMail?.id === mail.id;
          const isUnread = !mail.isRead;

          return (
            <div
              key={mail.id}
              onClick={() => onSelect(mail)}
              className={`mail-item 
                ${isSelected ? 'selected' : ''} 
                ${isUnread ? 'unread' : ''} 
                ${mail.spam ? 'spam' : ''}`}
            >
              <div className="mail-item-content">
                <div className="mail-item-meta">
                  <div className="mail-item-left">
                    <div className="mail-controls">
                      <input
                        type="checkbox"
                        checked={selectedMails.includes(mail.id)}
                        onChange={(e) => {
                          e.stopPropagation();
                          toggleSelectMail(mail.id);
                        }}
                        onClick={(e) => e.stopPropagation()}
                        className="mail-checkbox"
                      />
                      {mail.category !== 'trash' && (
                        <button
                          className="star-button"
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleStar(mail.id, mail.isStarred);
                          }}
                        >
                          <i
                            className={`bi ${mail.isStarred ? 'bi-star-fill' : 'bi-star'} star-icon`}
                            style={{ fontSize: '16px' }}
                          ></i>
                        </button>
                      )}
                    </div>
                    <span
                      className={`mail-sender ${isUnread ? 'bold' : ''}`}
                      dangerouslySetInnerHTML={{
                        __html: highlightSearch(
                          mail.category === 'inbox' ? (mail.fromMail || 'Unknown') : (mail.toMail || 'Unknown'),
                          searchTerm
                        ),
                      }}
                    ></span>
                    <div className="mail-main-text">
                      <span
                        className={`mail-subject ${isUnread ? 'bold' : ''}`}
                        dangerouslySetInnerHTML={{
                          __html: highlightSearch(mail.subject, searchTerm),
                        }}
                      ></span>

                      <span
                        className="mail-preview"
                        dangerouslySetInnerHTML={{
                          __html: highlightSearch(mail.preview, searchTerm),
                        }}
                      ></span>
                    </div>
                  </div>

                  <div className="mail-item-right">
                    <span
                      className="mail-date"
                      dangerouslySetInnerHTML={{
                        __html: highlightSearch(
                          new Date(mail.timestamp).toLocaleString("en-US", {
                            dateStyle: "medium",
                            timeStyle: "short",
                          }),
                          searchTerm
                        ),
                      }}
                    />
                    {mail.isImportant && <div className="important-dot"></div>}
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default MailList;
