import { useState, useEffect, useRef } from 'react';
import Sidebar from './Sidebar';
import MailList from './MailList';
import MailView from './MailView';
import ComposeModal from './ComposeModal';
import UserProfileModal from './UserProfileModal';
import '../styles/MailHome.css';

const defaultCategories = [
  { name: "inbox", iconClass: "bi-inbox", color: "blue", count: 0 },
  { name: "starred", iconClass: "bi-star", color: "yellow", count: 0 },
  { name: "sent", iconClass: "bi-send", color: "green", count: 0 },
  { name: "drafts", iconClass: "bi-file-earmark-text", color: "gray", count: 0 },
  { name: "spam", iconClass: "bi-exclamation-circle", color: "orange", count: 0 },
  { name: "trash", iconClass: "bi-trash", color: "red", count: 0 }
];

export default function MailHome() {
  const [mails, setMails] = useState([]); // all mails list
  const [selectedMail, setSelectedMail] = useState(null); // currently selected mail
  const [showCompose, setShowCompose] = useState(false); // state to control compose modal visibility
  const [selectedCategory, setSelectedCategory] = useState('inbox'); // selected category
  const [error, setError] = useState(""); // error message state
  const [loading, setLoading] = useState(false); // loading state
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false); // state to control sidebar visibility
  const [darkMode, setDarkMode] = useState(false); // dark mode state
  const [categories, setCategories] = useState(defaultCategories); // categories state
  const [labels, setLabels] = useState([]); // labels state
  const [searchInput, setSearchInput] = useState(''); // search input state
  const [searchTerm, setSearchTerm] = useState(''); // search term state
  const [currentPage, setCurrentPage] = useState(1); // current page for pagination
  const [totalPages, setTotalPages] = useState(1); // total pages for pagination
  const [selectedLabel, setSelectedLabel] = useState(''); // selected label for filtering mails

  // User menu states
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [userInfo, setUserInfo] = useState({
    name: "User Example",
    email: "user@example.com",
    avatar: null,
    username: "",
    birthDate: "",
    gender: ""
  });
// Function to handle mail updates 
  const handleUpdateMail = (updatedMail) => {
  setMails(prev =>
    prev.map(mail => (mail.id === updatedMail.id ? updatedMail : mail))
  );
  setSelectedMail(updatedMail);
};
  
  const userMenuRef = useRef(null);
  useEffect(() => {
  fetchMails(currentPage);
}, [currentPage]);

// Function to fetch mails from the server
  async function fetchMails(page = 1) {
  setLoading(true);
  setError(null);
  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found. Please login.");

    const res = await fetch(`http://localhost:4000/api/mails?page=${page}`, {      
      headers: {
        Authorization: `Bearer ${token}`,
      },
      credentials: 'include',
    });

    if (!res.ok) {
      throw new Error(`Failed to fetch mails: ${res.status}`);
    }

    const data = await res.json();
    setMails(data.mails);
    setCurrentPage(data.currentPage);
    setTotalPages(data.totalPages);
    
  
    if (selectedMail) {
      const updatedMail = data.find(mail => mail.id === selectedMail.id);
      setSelectedMail(updatedMail || null);  
    }
    
  } catch (err) {
    setError(err.message);
    setMails([]);
  } finally {
    setLoading(false);
  }
}


  // Handle new mail function
function handleNewMail(newMail) {
  // check if newMail is a valid object
  if (newMail && typeof newMail === 'object' && newMail.id) {
    setMails(prevMails => [newMail, ...prevMails]);
  } else {
    // if newMail is not valid, log an error and refresh all mails
    fetchMails();
  }
}

  // Close user menu when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target)) {
        setShowUserMenu(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Fetch user info
  useEffect(() => {
    async function fetchUserInfo() {
      try {
        const token = localStorage.getItem("token");
        const userId = localStorage.getItem("userId"); 

        if (!token || !userId) return;

        const res = await fetch(`http://localhost:4000/api/users/${userId}`, {
          headers: {
            Authorization: `Bearer ${token}`,
            'user-id': userId
          },
        });

        if (res.ok) {
          const userData = await res.json();

          const profileImagePath = userData.profileImage ? userData.profileImage.replace(/\\/g, '/') : null;
            const avatarUrl = profileImagePath ? `http://localhost:4000/${profileImagePath}` : null;

          setUserInfo({
            name: userData.first_name && userData.last_name
            ? `${userData.first_name} ${userData.last_name}`
            : "User",
            email: userData.mail || "user@example.com",
            avatar: userData.profileImage ? `http://localhost:4000/${userData.profileImage.replace(/\\/g, '/')}` : null,
            username: userData.username,
            birthDate: userData.birth_date,
            gender: userData.gender
          });
        }
      } catch (err) {
        console.error("Failed to fetch user info:", err);
      }
    }

    fetchUserInfo();
  }, []);

  // Handle logout
  const handleLogout = () => {
    localStorage.removeItem("token");
    setShowUserMenu(false);
    // Add navigation to login page or refresh
    window.location.href = "/login";
  };

  // Handle profile edit
  const handleEditProfile = () => {
    setShowUserMenu(false);
    setShowProfileModal(true);
  };

  // Handle user update from profile modal
  const handleUserUpdate = (updatedUserInfo) => {
    setUserInfo(updatedUserInfo);
  };

  
  // fetch mails from the server
  useEffect(() => {
    fetchMails();
  }, []);

useEffect(() => {
  // update categories count based on user mails

  if (!userInfo) return; // check if there is a user

  const userMails = mails.filter(mail => mail.toMail === userInfo.email);
  const userDraft = mails.filter(mail => mail.fromMail === userInfo.email);

  const updatedCategories = defaultCategories.map(category => {
    let count = 0;

    if (category.name === 'inbox') {
      count = userMails.filter(mail => mail.category === 'inbox' && mail.isRead === false).length;
    } else if (category.name === 'spam') {
      count = userMails.filter(mail => mail.category === 'spam' && mail.isRead === false).length;
    } else if (category.name === 'drafts') {
      count = userDraft.filter(mail => mail.category === 'drafts').length;
    }
    return { ...category, count };
  });

  setCategories(updatedCategories);

}, [mails, userInfo]);


function getCurrentUserId() {
  const token = localStorage.getItem("token");
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.userId;
  } catch {
    return null;
  }
}

function filterMailsByCategory() {
  const userId = getCurrentUserId();
  if (!userId) return [];

  let filtered = [];

  switch (selectedCategory) {
    case 'inbox':
      filtered = mails.filter(mail => mail.ownerId === userId && mail.category === 'inbox');
      break;
    case 'starred':
      filtered = mails.filter(mail => mail.ownerId === userId && mail.isStarred);
      break;
    case 'sent':
      filtered = mails.filter(mail => mail.ownerId === userId && mail.category === 'sent');
      break;
    case 'drafts':
      filtered = mails.filter(mail => mail.ownerId === userId && mail.category === 'drafts');
      break;
    case 'spam':
      filtered = mails.filter(mail => mail.ownerId === userId && mail.category === 'spam');
      break;
    case 'trash':
      filtered = mails.filter(mail => mail.ownerId === userId && mail.category === 'trash');
    default:
      filtered = mails;
  }
  // serch functionality - if searchTerm is not empty, filter mails by subject or body
  if (searchTerm.trim()) {
    const lower = searchTerm.toLowerCase();
    filtered = filtered.filter(mail => {
      const timestampString = new Date(mail.timestamp).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric'
      }).toLowerCase();

      return (
        (mail.subject && mail.subject.toLowerCase().includes(lower)) ||
        (mail.body && mail.body.toLowerCase().includes(lower)) ||
        (mail.fromMail && mail.fromMail.toLowerCase().includes(lower)) ||
        (mail.toMail && mail.toMail.toLowerCase().includes(lower)) ||
        timestampString.includes(lower)
      );
    });
  }

  return filtered;
}
// Function to toggle star status of a mail
const toggleStar = async (mailId, currentStarred) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found.");

    const res = await fetch(`http://localhost:4000/api/mails/${mailId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ isStarred: !currentStarred }),
    });

    if (!res.ok) {
      throw new Error("Failed to toggle star");
    }

    setMails((prevMails) =>
      prevMails.map((mail) =>
        mail.id === mailId ? { ...mail, isStarred: !currentStarred } : mail
      )
    );
  } catch (err) {
    console.error(err);
  }
};

// Function to move a mail to trash
const moveToTrash = async (mailId) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found.");

    // first, find the mail to move
    const mailToMove = mails.find(mail => mail.id === mailId);
    if (!mailToMove) throw new Error("Mail not found.");

    const res = await fetch(`http://localhost:4000/api/mails/${mailId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ 
        category: "trash", 
        previousCategory: mailToMove.category // save the previous category
      }),
    });

    if (!res.ok) {
      throw new Error("Failed to move mail to trash");
    }

    // if the update was successful, update the local state
    setMails((prevMails) =>
      prevMails.map((mail) =>
        mail.id === mailId 
          ? { 
              ...mail, 
              category: "trash", 
              isStarred: false, 
              previousCategory: mailToMove.category 
            } 
          : mail
      )
    );
  } catch (err) {
    console.error(err);
  }
};

// Function to restore a mail to its previous category from trash
const restoreMail = async (mailId, previousCategory) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found.");

    const res = await fetch(`http://localhost:4000/api/mails/${mailId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ category: previousCategory }),
    });

    if (!res.ok) {
      throw new Error("Failed to restore mail");
    }

    setMails((prevMails) =>
      prevMails.map((mail) =>
        mail.id === mailId
          ? { ...mail, category: previousCategory }
          : mail
      )
    );
    
    // close the mail view and reset selected category
    setSelectedMail(null);
    setSelectedCategory(previousCategory);
    
  } catch (err) {
    console.error(err);
  }
};

// Function to remove a mail from spam and get it out from the blacklist
const removeFromSpam = async (mail) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found.");

    const res = await fetch(`http://localhost:4000/api/mails/${mail.id}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ category: mail.previousCategory }),
    });

    if (!res.ok) {
      throw new Error("Failed to move mail from spam");
    }

    setMails((prevMails) =>
      prevMails.map((mailcheck) =>
        mailcheck.id === mail.id ? { ...mailcheck, category: mail.previousCategory } : mailcheck
      )
    );
    
      const res2 = await fetch(`http://localhost:4000/api/blacklist/${mail.id}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res2.ok) {
      throw new Error("Failed to remove mail from blacklist");
    }
  } catch (err) {
    console.error(err);
  }
};

// Function to move a mail to spam and add it to the blacklist
const moveToSpam = async (mail) => {
  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found.");

    const res = await fetch(`http://localhost:4000/api/mails/${mail.id}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ category: "spam", isStarred: false }),
    });

    if (!res.ok) {
      throw new Error("Failed to move mail to spam");
    }

    setMails((prevMails) =>
      prevMails.map((mailcheck) =>
        mailcheck.id === mail.id ? { ...mailcheck, category: "spam" , isStarred: false } : mailcheck
      )
    );
    
      const res2 = await fetch("http://localhost:4000/api/blacklist", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ subject: mail.subject, body: mail.body, }),
      });

      if (!res2.ok) {
      throw new Error("Failed to move mail to blacklist");
    }

    
  } catch (err) {
    console.error(err);
  }
};

// Function to handle permanent deletion of mails
const handlePermanentDelete = async (mailsToDelete) => {

  const mailsArray = Array.isArray(mailsToDelete) ? mailsToDelete : [mailsToDelete];

  const hasNonDraft = mailsArray.some(mail => mail.category !== 'drafts');

// If there are non-draft mails, confirm with the user before deletion
  if (hasNonDraft) {
    const confirmDelete = window.confirm("Are you sure you want to permanently delete the selected email(s)?");
    if (!confirmDelete) return;
  }

  try {
    const token = localStorage.getItem("token");
    if (!token) throw new Error("No token found.");

    for (const mail of mailsArray) {
      const res = await fetch(`http://localhost:4000/api/mails/${mail.id}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(`Failed to delete email: ${res.status} - ${errorText}`);
      }
    }

    setMails(prev => prev.filter(mail => !mailsArray.some(toDelete => toDelete.id === mail.id)));
    
    if (selectedMail && mailsArray.some(mail => mail.id === selectedMail.id)) {
      setSelectedMail(null);
    }

  } catch (err) {
    console.error("Delete error:", err);
    alert("Failed to delete one or more emails permanently: " + err.message);
  }
};



  return (
    <div className={`mail-home ${darkMode ? 'dark' : 'light'}`}>
      {/* Header */}
      <header className="header">
        <div className="header-content">
          <div className="left-header">
            <button
              onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
              className="menu-btn"
              aria-label="Toggle Sidebar"
            >
              <i className="bi bi-list" style={{ fontSize: '20px' }}></i>
            </button>
            <div className="logo">
              <img
                  src="https://ssl.gstatic.com/ui/v1/icons/mail/rfr/gmail.ico"
                  alt="Gmail Icon"
                  className="logo-image"
                />              
              <span className="logo-text">Gmail</span>
            </div>
          </div>

          <div className="search-wrapper">
            <button 
              onClick={() => setSearchTerm(searchInput)} 
              className="search-icon-button" 
              aria-label="Search"
            >
              <i className="bi bi-search" style={{ fontSize: '20px' }}></i>
            </button>
            <input
              type="text"
              placeholder="Search mail..."
              className="search-input"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  setSearchTerm(searchInput);
                }
              }}
            />
          </div>

          <div className="right-header">
            <button
              onClick={() => setDarkMode(!darkMode)}
              className="icon-btn"
              aria-label="Toggle Dark Mode"
            >
              {darkMode ? <i className="bi bi-sun" style={{ fontSize: '20px' }}></i> : <i className="bi bi-moon" style={{ fontSize: '20px' }}></i>}
            </button>
            
            
            {/* User Menu */}
            <div className={`user-menu-container ${darkMode ? 'dark' : ''}`} ref={userMenuRef}>
              <button
                onClick={() => setShowUserMenu(!showUserMenu)}
                className="user-avatar"
                aria-label="User Menu"
              >
                {userInfo.avatar ? (
                  <img src={userInfo.avatar} alt="User Avatar" />
                ) : (
                  userInfo.name.charAt(0).toUpperCase()
                )}
              </button>

              {showUserMenu && (
                <div className={`user-dropdown ${darkMode ? 'dark' : ''}`}>
                  <div className="user-info">
                    <div className="user-details">
                      <h3>{userInfo.username}</h3>
                      <p>{userInfo.email}</p>
                    </div>
                  </div>
                  
                  <div className="user-menu-divider"></div>
                  
                  <div className="user-menu-items">
                    <button
                      onClick={handleEditProfile}
                      className="user-menu-item"
                    >
                      <i className="bi bi-person" style={{ fontSize: '16px' }}></i>
                      <span>Edit profile</span>
                    </button>
                    
                    <button
                      onClick={handleLogout}
                      className="user-menu-item logout"
                    >
                      <i className="bi bi-box-arrow-right" style={{ fontSize: '16px' }}></i>
                      <span>Log out</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
  {/* Sidebar*/}
  {!sidebarCollapsed && (
    <Sidebar
      onCompose={() => setShowCompose(true)}
      selectedCategory={selectedCategory}
      onCategorySelect={(category) => {
        setSelectedCategory(category);
        setSelectedLabel('');
      }}
      setSelectedCategory={setSelectedCategory}
      setMails={setMails}
      categories={categories}
      labels={labels}
      darkMode={darkMode}
      selectedLabel={selectedLabel}
      onLabelSelect={(labelName) => {
        setSelectedLabel(labelName);
        setSelectedCategory('');
      }} />
      )}
        {/* Mail content */}
        <div className="mail-content">
          {!selectedMail && mails.length > 0  ? (
            <MailList
              mails={filterMailsByCategory()}
              loading={loading}
              error={error}
              selectedMail={selectedMail}
              onSelect={setSelectedMail}
              darkMode={darkMode}
              selectedCategory={selectedCategory}
              onRefresh={fetchMails}
              toggleStar={toggleStar}
              moveToTrash={moveToTrash}
              handlePermanentDelete={handlePermanentDelete}
              currentPage={currentPage}
              totalPages={totalPages}
              setCurrentPage={setCurrentPage}
              setMails={setMails}
              selectedLabel={selectedLabel}
              searchTerm={searchTerm}
            />
          ) : (
            <MailView 
              mail={selectedMail}
              darkMode={darkMode}
              restoreMail={restoreMail}
              moveToSpam={moveToSpam}
              onClose={() => setSelectedMail(null)}
              handlePermanentDelete={handlePermanentDelete}
              selectedCategory={selectedCategory}
              onUpdateMail={handleUpdateMail}
              setSelectedMail={setSelectedMail}         
              setSelectedCategory={setSelectedCategory}  
              removeFromSpam={removeFromSpam}
              
            />
          )}
        </div>

        {showCompose && (
          <ComposeModal 
            onClose={() => setShowCompose(false)} 
            darkMode={darkMode} 
            onMailSent={handleNewMail} 
          />        
        )}

        {showProfileModal && (
          <UserProfileModal 
            onClose={() => setShowProfileModal(false)} 
            darkMode={darkMode}
            userInfo={userInfo}
            onUserUpdate={handleUserUpdate}
          />
        )}
      </main>

    </div>
  );
}