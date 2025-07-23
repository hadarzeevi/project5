# 🔐 Authentication – Register & Login

## 📝 Registration Process

- The user fills out a registration form with username, email, password, and additional details  
- Client-side validation is performed (in both React and Android apps)  
- Data is sent to the server via `POST /api/users/register`  
- Password is hashed before being stored in the database  
- Upon success, the user is either redirected to the login screen or logged in automatically  

---

## 🔑 Login Process

- The user enters their username and password  
- Credentials are sent via `POST /api/users/login`  
- On successful authentication, the server returns a JWT token  
- The token is securely stored:
  - In React: `localStorage`
  - In Android: `SharedPreferences`  
- All future API requests include the token in the `Authorization` header as:  
  `Bearer <token>`

---

## 🛡️ Route Protection

- Protected routes are secured using the `authenticateToken.js` middleware  
- If a token is invalid or missing, the server returns `401 Unauthorized`  
- Unauthorized users are redirected to the login screen  

---

## 👤 User Profile Management

- Users can update personal information and upload a profile picture  
- The upload process includes image preview and file validation  
- Changes are saved to the backend and persisted in the database  

---

## 🔒 Security Notes

- JWTs are signed using the secret key `'secretkeyHHE'`  
- All protected endpoints validate the JWT on each request  
- HTTPS is strongly recommended in production environments

<img width="389" height="836" alt="צילום מסך 2025-07-23 181118" src="https://github.com/user-attachments/assets/fc90a970-f748-4bc0-a7a2-7e88d008ab8e" />
<img width="355" height="817" alt="צילום מסך 2025-07-23 175433" src="https://github.com/user-attachments/assets/fd6a5be2-1599-4c3a-8af1-eb1c61442600" />
<img width="318" height="570" alt="צילום מסך 2025-07-23 170631" src="https://github.com/user-attachments/assets/d09eb8f8-71e3-490a-827e-c5b1f63a2bca" />
