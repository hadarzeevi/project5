import { useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import LoginScreen from "./components/LoginScreen";
import Register from "./components/Register";
import MailHome from "./components/MailHome"; 


function App() {
  const [userId, setUserId] = useState(null);

  const handleLogin = (id) => setUserId(id);

  return (
    <div className="App">
      <Routes>
        {!userId ? (
          <>
            <Route path="/login" element={<LoginScreen onLogin={handleLogin} />} />
            <Route path="/register" element={<Register />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </>
        ) : (
          <>
            <Route path="/" element={<Navigate to="/mails" replace />} />
            <Route path="/mails" element={<MailHome />} />
            <Route path="*" element={<Navigate to="/mails" replace />} />
          </>
        )}
      </Routes>
    </div>
  );
}

export default App;
