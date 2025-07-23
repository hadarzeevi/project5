# Project 5 - Advanced Email System

## Overview

This project extends **Project 3** by adding a full-featured **React** web client and an **Android** client for managing users, emails, labels, and spam filtering.  
The backend consists of a **Node.js API server** communicating with a **C++ TCP blacklist server**.  
All components operate in Docker containers, and the Android app connects directly to the backend API.  

---

## Architecture

┌─────────────────────┐       ┌────────────────────────────┐       ┌─────────────────────────────┐
│  React Client (3000)│ ───►  │ Node.js API Server (4000)  │ ◄──► │ C++ TCP Blacklist Server    │
│                     │       │                            │       │        (8080)              │
└─────────────────────┘       └────────────────────────────┘       └─────────────────────────────┘
              ▲
              │
    ┌─────────────────┐
    │ Android Client  │
    │ (Android Studio)│
    └─────────────────┘

- React frontend communicates with Node.js via REST API  
- Android app communicates with Node.js backend via REST API  
- Node.js backend communicates with C++ blacklist server via raw TCP socket  
- MongoDB stores all persistent data  
- All backend services run in Docker containers  

---

## Features

### General
- Modular Android UI using Activities, Fragments, ViewModels, and LiveData
- Light/Dark theme toggle with persistent user preferences
- Single-Activity architecture with smooth navigation (no full app reloads)
- Responsive layout optimized for various screen sizes (Gmail-style interface)

### Authentication
- Register & log in with comprehensive form validation on mobile
- JWT-based session management with secure token storage (SharedPreferences)
- Auth-protected screens with automatic redirect to login if unauthenticated
- Profile management with image upload and preview using device gallery or camera

### Email Management
- **Compose & Send**: Rich text editor with file attachment support
- **Read & Organize**: Mark as read/unread, star/unstar emails
- **Categories**: Inbox, Sent, Drafts, Starred, Spam, Trash
- **Delete Operations**: Soft delete (move to trash) and permanent deletion
- **Restore**: Recover emails from trash to original location
- **Pagination**: 50 emails per page with navigation controls
- **Search**: Advanced search by subject, content, sender, date, recipient
- **Reply & Forward**: Support for replying and forwarding emails with attachments
- **Draft Management**: Auto-save to drafts when canceling compose

### File Attachments
- **Upload Support**: Multiple file attachments with progress tracking
- **File Validation**: Size limits (10MB max) and type validation
- **Attachment Management**: Remove/add attachments during compose
- **Forward Attachments**: Preserve attachments when forwarding emails
- **Download Support**: Download attachments from received emails

### Labels System
- Create, edit, and delete custom labels
- Apply multiple labels to emails
- Filter emails by labels
- Bulk label operations
- Color-coded label system

### Spam Filtering
- **Real-time Blacklist Check**: Integration with C++ blacklist server via TCP
- **Automatic Spam Detection**: Emails with blacklisted URLs automatically marked as spam
- **Manual Spam Management**: Users can mark emails as spam (adds URLs to blacklist)
- **Spam Recovery**: Spam emails can be restored and URLs removed from blacklist
- **Blacklist Persistence**: Blacklist data persists across server restarts

### User Profile
- Profile modal with editable fields (name, username, birth date, gender)
- Profile image upload with preview and file validation
- Account settings and preferences
- Secure logout functionality

---

## Running the System

### Step 1: Run Backend Services
```bash
docker-compose up --build
```
## Step 2: Run the Android Client

* Open the Android project in **Android Studio**.
* Run the app on an emulator or physical device.
* The Android app communicates with the Node.js API server running on localhost or your server IP.
* Make sure the backend (Docker services) is running before starting the Android app.

---

## Accessing the Application

* **Web client:** Open [http://localhost:3000](http://localhost:3000) in your browser.
* **Android client:** Use the Android app installed via Android Studio emulator or device.

---

## Multi-User Functionality

* Supports multiple users, each with isolated sessions.
* Users can register and log in on **either** client (web or Android).
* Emails, labels, and profiles are user-specific and synchronized across clients.
* Changes on one client instantly reflect on the other.
* Supports simultaneous use on multiple devices or browsers.

  ---
### Security Considerations
- JWT token validation through `authenticateToken.js` middleware
- Uses hardcoded secret key `'secretkeyHHE'` for token verification
- Bearer token authentication on protected routes with 401/403 error handling
- Basic JWT signature verification to prevent token tampering


## Project Structure
```
Project5/
├── app/                             # Android Studio app source code
│   └── src/
│       ├── androidTest/             # Instrumented tests
│       │   └── java/
│       │       └── com.example.project5/
│       ├── main/
│       │   ├── java/                # Java/Kotlin source files
│       │   │   └── com.example.project5/
│       │   │       ├── model/       # Data models
│       │   │       └── ui/          # UI components
│       │   │           └── theme/   # Theme and style related files
│       │   └── res/                 # Resources: layouts, drawables, colors, mipmaps, values, xml etc.
│       └── test/                    # Unit tests
│           └── java/
│               └── com.example.project5/
│
├── gradle/                         # Gradle wrapper files
│
├── Part1/                         # Backend Services
│   ├── project2/                  # C++ blacklist server
│   │   ├── src/
│   │   │   └── data/
│   │   └── tests/
│   └── project3/                  # Node.js API server
│       ├── controllers/
│       ├── middleware/            # Authentication middleware (e.g., JWT validation)
│       ├── models/                # Database models
│       ├── routes/                # API routes
│       ├── services/              # Business logic services
│       └── uploads/
│           └── files/             # File storage for uploads
│
└── Part2/                         # Frontend Web Client
└── client/
├── public/                # Static assets for React frontend
└── src/
├── components/        # React components
├── hooks/             # Custom React hooks
└── styles/            # CSS stylesheets

```

## Notes

* The Android app uses the **same API endpoints** as the React frontend.
* **No Docker container is used for Android Studio**; it runs locally or on an emulator/device.
* Ensure network connectivity between the Android device/emulator and the backend server.
* For Android emulator, use IP `10.0.2.2` to access the backend on your local machine if needed.

---

## Screenshots


---
![PHOTO-2025-07-23-18-00-37](https://github.com/user-attachments/assets/1570f687-8d05-4987-bbd0-eebec4e58601)
![PHOTO-2025-07-23-18-00-33 (1)](https://github.com/user-attachments/assets/f49a34ea-e826-44f1-9340-b1296857eff1)
![PHOTO-2025-07-23-18-00-33](https://github.com/user-attachments/assets/09cbe73f-1f6d-4de1-a39f-f54c4455cbd0)
![PHOTO-2025-07-23-18-00-32 (2)](https://github.com/user-attachments/assets/d2c09c68-22a9-4349-aa8f-1260eb614098)
![PHOTO-2025-07-23-18-00-32](https://github.com/user-attachments/assets/f6845e4c-d09d-4413-b0a5-cefd5e7e80f5)
![PHOTO-2025-07-23-18-00-32 (1)](https://github.com/user-attachments/assets/fd45ad7d-7f1f-4e9d-b098-df7835077070)
