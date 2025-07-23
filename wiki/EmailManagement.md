# Email Management - Creating, Editing, and Deleting Emails

## Composing and Sending Emails

- Rich text compose interface with file attachment support  
- Ability to save drafts or send emails immediately  
- POST request to `/api/mails` with email content and attachments  

---

## Reading and Organizing Emails

- View emails by category: Inbox, Sent, Drafts, Starred, Spam, Trash  
- Mark emails as read/unread  
- Star or unstar emails for importance  
- Open and read individual emails with reply/forward functionality  

---

## Deleting and Restoring Emails

- Soft delete moves emails to Trash folder  
- Permanent deletion removes emails from the database  
- Emails can be restored from Trash to their original folder  
- API endpoints support these operations  

---

## Filtering and Pagination

- Advanced search by subject, content, sender, date, recipient  
- Sort emails by date, importance, and more  
- Pagination with 50 emails per page  

---

## Attachments Handling

- Upload multiple attachments with validation (max size 10MB)  
- Attachments can be removed or added during composing  
- Download attachments from received emails  
- Files stored on server under `/uploads/files/`  

---
<img width="333" height="742" alt="צילום מסך 2025-07-23 175618" src="https://github.com/user-attachments/assets/9017ac88-1ec1-4d73-82d7-39590e692f26" />
<img width="355" height="759" alt="צילום מסך 2025-07-23 175740" src="https://github.com/user-attachments/assets/eae3504b-da07-48bd-861f-a5188493d5c3" />
<img width="341" height="726" alt="צילום מסך 2025-07-23 180007" src="https://github.com/user-attachments/assets/80fcedd2-56b5-4091-8d15-c7d7b0af5b3c" />
<img width="366" height="800" alt="צילום מסך 2025-07-23 181204" src="https://github.com/user-attachments/assets/3518053a-c9c5-455f-bafc-c2bc538ee515" />
<img width="365" height="790" alt="צילום מסך 2025-07-23 181356" src="https://github.com/user-attachments/assets/0d79d59d-d12d-4bbc-b009-4bf974f02c1e" />

## REST API Examples

- Send a new email:

```http
POST /api/mails
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  to: "recipient@example.com",
  subject: "Hello",
  body: "This is a test email.",
  attachments: [file1, file2]
}

- Get email list:

GET /api/mails?folder=inbox&page=1
Authorization: Bearer <token>

- Delete an email:

DELETE /api/mails/:mailId
Authorization: Bearer <token>




