import { useState, useEffect, useRef } from 'react';
import '../styles/LabelDropdown.css';

// This component displays a dropdown for selecting labels to apply to an email.
function LabelDropdown({ 
  isOpen, 
  onClose, 
  onAddToLabel, 
  onRemoveFromLabel,
  currentLabels = [],
  position = 'bottom-left',
  darkMode
}) {
  const [labels, setLabels] = useState([]);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (isOpen) fetchLabels();
  }, [isOpen]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        onClose();
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen, onClose]);

// Fetch labels from the server when the dropdown is opened
  const fetchLabels = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:4000/api/labels", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.ok) {
        const data = await response.json();
        setLabels(data);
      } else {
        console.error("Failed to fetch labels", response.status);
      }
    } catch (err) {
      console.error("Error fetching labels:", err);
    }
  };

// get a label, if it exists it will be removed, otherwise it will be added
  const handleLabelToggle = (labelname) => {
    if (currentLabels.includes(labelname)) {
      onRemoveFromLabel(labelname);
    } else {
      onAddToLabel(labelname);
    }
  };

  if (!isOpen) return null;

  const positionClasses = {
    'bottom-left': 'top-full left-0',
    'bottom-right': 'top-full right-0',
    'top-left': 'bottom-full left-0',
    'top-right': 'bottom-full right-0'
  };

  
  
  return (
    <div
      ref={dropdownRef}
      className={`label-dropdown ${darkMode ? 'dark' : ''}`}
      onClick={(e) => e.stopPropagation()}
    >
      {/* Header */}
      <div className="label-dropdown-header">
      <span className="label-dropdown-title">Labels</span>
      <button onClick={onClose} className="label-dropdown-close">
          <i className="bi bi-x" style={{ fontSize: '16px' }}></i>
        </button>
      </div>

      {/* Labels */}
      <div className="label-dropdown-list">
      {labels.length === 0 ? (
        <div className="label-dropdown-empty">
            No labels available
          </div>
        ) : (
          labels.map(label => {
            const isApplied = currentLabels.includes(label.name);
            return (
              <button
                key={label.name}
                onClick={() => handleLabelToggle(label.name)}
              className="label-dropdown-item"
              >
                <div
                className="label-dropdown-color"
                  style={{ backgroundColor: label.color }}
                ></div>
              <span className="label-dropdown-label">{label.name}</span>
              {isApplied && <i className="bi bi-check" style={{ fontSize: '16px' }}></i>}
              </button>
            );
          })
        )}
      </div>
    </div>
  );
}

export default LabelDropdown;
