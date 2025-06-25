import React, { useState, useEffect } from 'react';

import '../styles/UserProfileModal.css';
const UserProfileModal = ({ onClose, darkMode, userInfo, onUserUpdate }) => {
  const [formData, setFormData] = useState({
    first_name: '',
    last_name: '',
    username: '',
    mail: '',
    birth_date: '',
    gender: '',
    profileImage: null
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [imagePreview, setImagePreview] = useState(userInfo.avatar || null);

useEffect(() => {
  if (userInfo) {
    const fixedAvatarUrl = userInfo.avatar
      ? `http://localhost:4000/${userInfo.avatar.replace(/\\/g, '/')}`
      : null;

    // if the picture is not set, use a default picture
    setImagePreview(prev => prev || fixedAvatarUrl);

    const nameParts = userInfo.name.split(' ');
    setFormData({
      first_name: nameParts[0] || '',
      last_name: nameParts.slice(1).join(' ') || '',
      username: userInfo.username || '',
      mail: userInfo.mail || '',
      birth_date: userInfo.birthDate ? userInfo.birthDate.split('T')[0] : '',
      gender: userInfo.gender || '',
      profileImage: null
    });
  }
}, [userInfo]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Handle image change
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setFormData(prev => ({
        ...prev,
        profileImage: file
      }));
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setImagePreview(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");
      
      if (!token || !userId) {
        throw new Error("No eccess permission");
      }

      const formDataToSend = new FormData();
      Object.keys(formData).forEach(key => {
        if (key === 'profileImage' && formData[key]) {
          formDataToSend.append('profileImage', formData[key]);
        } else if (key !== 'profileImage') {
          formDataToSend.append(key, formData[key]);
        }
      });

      const res = await fetch(`http://localhost:4000/api/users/${userId}`, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'user-id': userId
        },
        body: formDataToSend
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.error || 'Error updating profile');
      }

      const updatedUser = await res.json();
      setSuccess('Profile update successfully!');
      
      // Update parent component
      if (onUserUpdate) {
        onUserUpdate({
          name: `${formData.first_name} ${formData.last_name}`,
          mail: formData.mail,
          avatar: updatedUser.avatar || imagePreview,
          username: formData.username,
          birthDate: formData.birth_date,
          gender: formData.gender
        });
      }

      setTimeout(() => {
        onClose();
      }, 1500);

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`modal-overlay ${darkMode ? 'dark' : ''}`} onClick={onClose}>
      <div 
        className={`profile-modal ${darkMode ? 'dark' : ''}`}
        onClick={e => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2>Edit profile</h2>
          <button onClick={onClose} className="close-btn">
            <i className="bi bi-x" style={{ fontSize: '16px' }}></i>

          </button>
        </div>

        <form onSubmit={handleSubmit} className="profile-form">
          {/* Profile Image */}
          <div className="image-preview">
            {imagePreview ? (
              <img src={imagePreview} alt="Profile" />
            ) : (
              <span>?</span>
            )}
          </div>
          <div className="image-section">
            
            <label className="image-upload-btn">
              <i className="bi bi-camera" style={{ fontSize: '16px' }}></i>
              <span>Change picture</span>
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                hidden
              />
            </label>
          </div>

          {/* Form Fields */}
          <div className="form-grid">
            <div className="form-group">
              <label>First name</label>
              <input
                type="text"
                name="first_name"
                value={formData.first_name}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Last name</label>
              <input
                type="text"
                name="last_name"
                value={formData.last_name}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Birth date</label>
              <input
                type="date"
                name="birth_date"
                value={formData.birth_date}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Gender</label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleInputChange}
                required
              >
                <option value="">Select a gender</option>
                <option value="male">Male</option>
                <option value="female">Female</option>
              </select>
            </div>
          </div>

          {error && (
            <div className="error-message">
              <i className="bi bi-exclamation-circle" style={{ fontSize: '16px' }}></i>
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="success-message">
              <span>{success}</span>
            </div>
          )}

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="cancel-btn">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="save-btn">
              {loading ? (
                <span>Save...</span>
              ) : (
                <>
              <i className="bi bi-save" style={{ fontSize: '16px' }}></i>
                  <span>Save changes</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserProfileModal;