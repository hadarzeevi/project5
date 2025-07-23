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




