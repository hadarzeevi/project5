import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/LoginScreen.css";

export default function LoginScreen({ onLogin }) {
  const [mail, setMail] = useState("");
  const [password, setPassword] = useState("");
  const [step, setStep] = useState("email"); // "email" or "password"
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [userProfile, setUserProfile] = useState(null);
  const navigate = useNavigate();

    // function to handle email submission, it sends the email to the server to verify if it exists
  const handleEmailSubmit = async (e) => {
    e.preventDefault();
    setError("");
    
    try {
      const res = await fetch("http://localhost:4000/api/tokens/verify-mail", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mail }),
      });
      
      if (res.ok) {
        try {
          const userData = await res.json();
          if (userData && typeof userData === 'object') {
            setUserProfile(userData);
          }
        } catch (err) {
        }
        setStep("password");
      } else {
        setError("Email not found");
      }
    } catch (err) {
      setError("Connection error");
    }
  };

  // function to handle password submission, it sends the email and password to the server to get a token
  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const res = await fetch("http://localhost:4000/api/tokens", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ mail, password }),
    });

    if (res.ok) {
      const data = await res.json();
      localStorage.setItem("token", data.token);
      localStorage.setItem("userId", data.userId);
      onLogin(data.userId);
    } else {
      setError("Incorrect password");
    }
  };

  const getDisplayName = () => {
    if (!userProfile) return mail.split('@')[0];
    
    if (userProfile.first_name) {
      return userProfile.first_name;
    } else if (userProfile.username) {
      return userProfile.username;
    } else {
      return mail.split('@')[0];
    }
  };

  const getProfileImage = () => {
    if (userProfile && userProfile.avatar) {
      return `http://localhost:4000/${userProfile.avatar.replace(/\\/g, '/')}`;
    }
    return null;
  };

 return (
    <div className="google-register-page">
      <div className="form-container">
        {/* צד ימין */}
        <div className="side-panel">
          <img
            src="https://www.gstatic.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"
            alt="Google"
            className="side-logo"
          />
          <h1>Sign in</h1>
          <p>to continue to Gmail</p>
        </div>

        {/* צד שמאל */}
        <form onSubmit={step === "email" ? handleEmailSubmit : handlePasswordSubmit}>
          {step === "email" ? (
            <>
              <input
                type="email"
                placeholder="Email"
                value={mail}
                onChange={(e) => setMail(e.target.value)}
                required
              />
              {error && <div className="error-text">{error}</div>}
              <div className="form-actions">
                <button type="button"
                className="create-account-btn"
                onClick={() => navigate("/register")}>
                  Create account
                </button>
                <button type="submit" className="next-btn">
                  Next
                  </button>
              </div>
            </>
          ) : (
            <>
              <div>
                <h2 className="sign-in-title">Hi</h2>
                <h2 className="user-email">{mail}</h2>
              </div>
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <label className="show-password-inline">
                <input
                  type="checkbox"
                  checked={showPassword}
                  onChange={(e) => setShowPassword(e.target.checked)}
                />
                Show password
              </label>
              {error && <div className="error-text">{error}</div>}
              <div className="buttons">
                <button type="button" onClick={() => setStep("email")}>
                  Back
                </button>
                <button type="submit">Sign in</button>
              </div>
            </>
          )}
        </form>
      </div>
    </div>
  );
}

