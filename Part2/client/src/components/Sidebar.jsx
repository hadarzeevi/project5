import '../styles/Sidebar.css';
import React, { useState, useEffect } from 'react';
import LabelDropdown from './LabelDropdown';


function Sidebar({ onCompose, selectedCategory, setSelectedCategory, onCategorySelect, categories, darkMode, onLabelSelect, selectedLabel, setMails }) {
  const [labels, setLabels] = useState([]);
  const [allMails, setAllMails] = useState([]);

  const fetchLabels = async () => {
        try {
          const token = localStorage.getItem("token");
          const response = await fetch("http://localhost:4000/api/labels", {
            headers: {
              Authorization: `Bearer ${token}`,
            },
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
  // fetch all labels when the component mounts
  useEffect(() => {
    fetchLabels();
  }, []);

  const handleSelectLabel = (labelName) => {
    onCategorySelect(labelName);
  };

const handleAddLabel = async () => {
  const name = prompt("Enter label name:");
  const color = prompt("Enter label color (e.g. red, blue, #ffaa00):");

  if (!name || !color) return;

  try {
    const token = localStorage.getItem("token");
    const response = await fetch("http://localhost:4000/api/labels", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({ name, color })
    });

    if (response.ok) {
      // update the labels state with the new label
      const newLabels = await response.json();
      await fetchLabels();
    } else {
      const result = await response.json();
      if (response.status === 409) {
        alert(result.error); // "Label name already exists for this user"
      } else {
        console.error("Failed to add label", response.status, result);
      }
    }
  } catch (err) {
    console.error(err);
  }
};

// Function to handle label deletion
const handleDeleteLabel = async (labelName) => {
  const confirmDelete = window.confirm("Are you sure you want to permanently delete the selected label?");
  if (!confirmDelete) return;
  try {
    const labelToDelete = labels.find((l) => l.name === labelName);
    if (!labelToDelete) return;
    const token = localStorage.getItem('token');
    const allMailsRes = await fetch(`http://localhost:4000/api/mails`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
    });

    if (!allMailsRes.ok) {
      throw new Error("Failed to get mail");
    }
    const allMails = await allMailsRes.json();
    const mailsWithLabel = allMails.mails.filter(mail => mail.label?.includes(labelToDelete.name));
    for (const mail of mailsWithLabel) {
      const updatedLabelIds = mail.label.filter(name => name !== labelToDelete.name);
      const res2 = await fetch(`http://localhost:4000/api/mails/${mail.id}`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ label: updatedLabelIds }),
        });
      
        if (!res2.ok) {
          throw new Error("Failed to updat mail");
        }
        setMails((prevMails) =>
        prevMails.map((mailnew) =>
        mailnew.id === mail.id ? { ...mailnew, label: updatedLabelIds } : mailnew
      )
    );
    }

    const res3 = await fetch(`http://localhost:4000/api/labels/${labelToDelete.id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
        'user-id': labelToDelete.userId,
      },
    });

    if (!res3.ok) {
      throw new Error('Failed to delete label');
    }

    
// Update the labels state to remove the deleted label
    setLabels((prev) => prev.filter((l) => l.id !== labelToDelete.id));
    onLabelSelect(null); 
    setSelectedCategory('inbox'); // inbox as default category
  } catch (err) {
    console.error(err);
    alert('שגיאה במחיקת לייבל');
  }
};


  return (
      <div className={`sidebar ${darkMode ? 'dark' : 'light'}`}>
        <div className="compose-wrapper">
          <button onClick={onCompose} className="compose-btn">
            <i className="bi bi-pencil" style={{ fontSize: '18px' }}></i>

            Compose
          </button>
        </div>

        <div className="categories-wrapper">
          <nav>
            {categories.map(category => {
              const isSelected = selectedCategory === category.name;
              return (
                <button
                  key={category.name}
                  onClick={() => onCategorySelect(category.name)}
                  className={`category-btn ${isSelected ? 'selected' : ''}`}
                >
                  <div className="category-info">
                   <i className={`bi ${category.iconClass} category-icon`} style={{ fontSize: '18px' }}></i>
                    <span className="category-name">{category.name}</span>
                  </div>
                  <span className={`count ${category.count > 0 ? 'has-count' : ''}`}>
                    {category.count > 0 ? category.count : ''}
                  </span>
                </button>
              );
            })}
          </nav>

          {labels.length >= 0 && (
            <div className="labels-section">
              <div className="labels-header">
                <span className="labels-title">Labels</span>
                {selectedLabel && (
                  <button
                    className="delete-label-btn"
                    onClick={() => handleDeleteLabel(selectedLabel)}
                    title="Delete label"
                  >
                    <i className="bi bi-dash" style={{ fontSize: '22px' }}></i>

                  </button>
                )}
                <button className="add-label-btn" onClick={handleAddLabel}>
                  <i className="bi bi-plus" style={{ fontSize: '22px' }}></i>
                </button>
              </div>
            {labels.map(label => {
              const isSelectedLabel = selectedLabel === label.name;
              return (
              <button
                key={label.name}
                onClick={() => {
                  onCategorySelect(null);
                  onLabelSelect(label.name);
                }}
                className={`label-item ${isSelectedLabel ? 'selected' : ''}`}
              >
                <div
                  className="label-color"
                  style={{ backgroundColor: label.color }}
                ></div>
                <span className="label-name">{label.name}</span>
              </button>
              );
            })}
            </div>
          )}
        </div>
      </div>
  );
}

export default Sidebar;
