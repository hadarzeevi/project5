import '../styles/AttachmentsList.css';

function AttachmentsList({ 
  attachments, 
  onRemoveAttachment, 
  darkMode 
}) {
  // function to format file size
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  if (attachments.length === 0) {
    return null;
  }

  return (
    <div className={`attachments-section ${darkMode ? 'dark' : 'light'}`}>
      <div className="attachments-list">
        {attachments.map((file, index) => (
          <div key={index} className="attachment-item">
            <i className="bi bi-file" style={{ fontSize: '16px' }}></i>
            <span className="attachment-name">{file.name}</span>
            <span className="attachment-size">({formatFileSize(file.size)})</span>
            <button 
              className="remove-attachment icon-button"
              onClick={() => onRemoveAttachment(index)}
              type="button"
              title="Remove attachment"
            >
              <i className="bi bi-trash2" style={{ fontSize: '14px' }}></i>
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default AttachmentsList;