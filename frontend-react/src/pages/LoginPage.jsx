import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api.js";

function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    const token = btoa(username + ":" + password);

    try {
      const response = await api.get("/auth/me", {
        headers: { Authorization: "Basic " + token },
      });
      sessionStorage.setItem("authToken", token);
      sessionStorage.setItem("username", response.data.username);
      sessionStorage.setItem("role", response.data.role);
      navigate("/dashboard");
    } catch (err) {
      setError("Incorrect username or password.");
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <img src="/marca_UJA.png" alt="Universidad de Jaén" style={{ height: 60, marginBottom: 16 }} />
        <h4>Purchase Management</h4>
        <p className="text-muted">University of Jaén</p>

        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Username</label>
            <input
              className="form-control"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Password</label>
            <input
              type="password"
              className="form-control"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-uja w-100">Sign In</button>
        </form>

        <div className="text-center mt-3">
          <Link to="/register">No account? Register here</Link>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;