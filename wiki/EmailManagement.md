# 📬 Email Management – Creating, Editing & Deleting Emails

## 📝 Composing & Sending Emails

- Rich-text editor for writing emails  
- Support for multiple file attachments  
- Save as **draft** or **send immediately**  
- Uses `POST /api/mails` to submit email content and files  

---

## 📥 Reading & Organizing Emails

- Browse emails by category:  
  📥 Inbox · 📤 Sent · 📝 Drafts · ⭐ Starred · 🚫 Spam · 🗑️ Trash  
- Mark emails as **read** or **unread**  
- ⭐ Star / unstar emails for importance  
- View individual email with options to **Reply** or **Forward**

---

## 🗑️ Deleting & Restoring Emails

- **Soft delete** moves email to Trash  
- **Permanent delete** removes it from the database  
- Restore from Trash to original folder  
- All operations handled via API endpoints

---

## 🔎 Filtering & Pagination

- Filter emails by:  
  - Subject  
  - Body content  
  - Sender  
  - Recipient  
  - Date  
- Sort emails by **date**, **importance**, etc.  
- Pagination: 50 emails per page  

---

## 📎 Attachment Handling

- Upload multiple attachments (max total size: **10MB**)  
- Add or remove attachments during compose  
- Download attachments from received emails  
- Files stored on server under: `/uploads/files/`

---

## 🖼️ Screenshots
<img width="333" height="742" alt="Compose UI" src="https://github.com/user-attachments/assets/9017ac88-1ec1-4d73-82d7-39590e692f26" /> <img width="355" height="759" alt="Inbox UI" src="https://github.com/user-attachments/assets/eae3504b-da07-48bd-861f-a5188493d5c3" /> <img width="341" height="726" alt="Mail View" src="https://github.com/user-attachments/assets/80fcedd2-56b5-4091-8d15-c7d7b0af5b3c" /> <img width="366" height="800" alt="Reply View" src="https://github.com/user-attachments/assets/3518053a-c9c5-455f-bafc-c2bc538ee515" /> <img width="365" height="790" alt="Star/Trash" src="https://github.com/user-attachments/assets/0d79d59d-d12d-4bbc-b009-4bf974f02c1e" /> ```
