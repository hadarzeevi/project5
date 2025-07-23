# Setting Up the Entire Environment - Advanced Email System

## Overview
This document contains instructions to build and run the entire system:  
- Compile and run backend servers (C++ blacklist server & Node.js API server) using Docker  
- Run the React frontend  
- Run the Android application  

---

## Prerequisites

- Docker and Docker Compose installed and configured  
- Node.js and npm installed (for frontend)  
- Android Studio installed (for running Android app)  
- Network connectivity between development machine and backend server (if running remotely)  

---

## 1. Running Backend Services with Docker Compose

From the root project folder, run:

```bash
docker-compose up --build
```

This command will build and start the following Docker containers:

* C++ TCP blacklist server (port 8080)
* Node.js API server (port 4000)
* MongoDB database (port 27017)

### Verify Running Containers

After starting, check with:

```bash
docker ps
```

You should see the above services running and listening on their respective ports.

---

## 2. Running the React Web Client

* Navigate to the frontend folder (`Part2/client`)
* Install dependencies (if not done previously):

```bash
npm install
```

* Start the React client on port 3000:

```bash
npm start
```

* Open your browser and go to:

```
http://localhost:3000
```

---

## 3. Running the Android Application

* Open the Android project located in the `app` folder in Android Studio
* Select an emulator or connect a physical device
* Run the app (Run > Run 'app')

> **Important:**
> Make sure the Node.js API server is running and accessible from the emulator.
> For Android emulators, use IP `10.0.2.2` instead of `localhost` to reach your host machine.

---

## Summary

Once completed, the full system will be running:

* Backend servers inside Docker containers
* React web client in the browser
* Android app on emulator or device

You can now start testing authentication, sending emails, spam marking, and more.

````

---

```markdown
# Authentication - Register and Login

## Registration Process

- User fills registration form with username, email, password, and other details  
- Client-side validation in React and Android apps  
- Data sent to Node.js server endpoint: `/api/users/register`  
- Password is hashed before storing in database  
- On success, user is redirected to login or logged in automatically  

---

## Login Process

- User enters username and password  
- Credentials sent to server endpoint: `/api/users/login`  
- On successful authentication, server returns a JWT token  
- Token is stored securely:  
  - React: in LocalStorage  
  - Android: in SharedPreferences  
- Future API requests include the token in the `Authorization` header as `Bearer <token>`  

---

## Route Protection

- Protected routes use `authenticateToken.js` middleware  
- Invalid or missing tokens result in `401 Unauthorized` responses  
- Unauthorized users are redirected to login screen  

---

## User Profile Management

- Users can edit personal information and upload profile pictures  
- Profile image upload includes preview and file validation  
- Updated data is saved to the backend and database  

---

## Security Notes

- JWTs are signed with secret key `'secretkeyHHE'`  
- All secured API endpoints validate the JWT token  
- HTTPS is recommended in production environments  
````

---
<img width="1883" height="730" alt="צילום מסך 2025-07-23 181839" src="https://github.com/user-attachments/assets/c2b95760-68bc-47cb-ac07-5de933d73896" />

<img width="1456" height="693" alt="צילום מסך 2025-07-23 181859" src="https://github.com/user-attachments/assets/6ed70b55-fd3c-4f81-9d3d-320ec55d2b0f" />

