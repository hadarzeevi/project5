# Advanced Email System – Project 5 Wiki

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

## Running the System

### Step 1: Run Backend Services

Run the backend servers and MongoDB using Docker Compose:

```bash
docker-compose up --build
בטח! הנה הקובץ מסודר וברור, מוכן להעתקה — תוכל להשתמש בו ישירות ב־wiki:

---

# Running the Full Email System with Android Client

## Step 1: Run Backend Services with Docker Compose

Start all backend components together using Docker Compose:

* **C++ blacklist TCP server** (port 8080)
* **Node.js API server** (port 4000)
* **React frontend** (port 3000)
* **MongoDB** (port 27017)

Run this command in your project root:

```bash
docker-compose up --build
```

Ensure all containers start successfully before proceeding.

---

## Step 2: Run the Android Client

* Open the Android project in **Android Studio** (this is **not** containerized; run locally).
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

## Android Client – Features and User Actions

### Authentication

* Register new users with validation and optional profile image upload.
* Log in and securely store JWT token for session management.
* Log out clears session data and returns to login screen.

### Email Management

* View **Inbox**, **Sent**, **Drafts**, **Starred**, **Spam**, and **Trash** folders.
* Open emails to read full content, attachments, and metadata.
* Compose new emails with rich text and multiple attachments.
* Reply, reply all, and forward emails with attachment preservation.
* Mark emails as read/unread and star/unstar for importance.
* Delete emails (soft delete to Trash or permanent delete).
* Restore emails from Trash back to original folders.
* Search emails by subject, content, sender, recipient, and date.
* Pagination support for large mailboxes.

### Labels System

* Create, edit, and delete custom labels with colors.
* Assign multiple labels to emails and filter by labels.
* Bulk label operations on multiple selected emails.

### Spam Filtering

* Automatic spam detection via blacklist URLs (from C++ blacklist server).
* Manually mark/unmark emails as spam, updating blacklist accordingly.

### File Attachments

* Upload multiple files with size/type validation during compose.
* Remove attachments before sending.
* Download attachments from received emails.

### User Profile & Settings

* View and edit profile information (name, username, birth date, gender).
* Upload and preview profile image with validation.
* Toggle light/dark theme with persistent preference.

### Synchronization

* All data synced with backend and visible on all logged-in clients.
* Supports real-time updates and multi-device parallel usage.

---

## Notes

* The Android app uses the **same API endpoints** as the React frontend.
* **No Docker container is used for Android Studio**; it runs locally or on an emulator/device.
* Ensure network connectivity between the Android device/emulator and the backend server.
* For Android emulator, use IP `10.0.2.2` to access the backend on your local machine if needed.

---

## Screenshots


---

אם תרצה אוכל גם לעזור להכין את קובץ ה-Wiki בפורמט markdown מלא, או כל דבר אחר!
