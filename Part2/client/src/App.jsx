import { useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginScreen from "./components/LoginScreen";
import Register from "./components/Register";
import MailHome from "./components/MailHome";

function App() {
  const [userId, setUserId] = useState(null);

  return (
    <Router>
      <Routes>
        {!userId ? (
          <>
            <Route path="/login" element={<LoginScreen onLogin={setUserId} />} />
            <Route path="/register" element={<Register />} />
            {/* if someone try to get a diiferent path we will navigate him to login page */}
            <Route path="*" element={<Navigate to="/login" />} />
          </>
        ) : (
       <>
            <Route path="/mails" element={<MailHome />} />
            <Route path="*" element={<Navigate to="/mails" replace />} />
          </>
        )}
      </Routes>
    </Router>
  );
}

export default App;