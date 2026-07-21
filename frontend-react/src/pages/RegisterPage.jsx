import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import axios from "axios";

function RegisterPage() {
  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("TEACHER");
  const [departmentId, setDepartmentId] = useState("");
  const [departments, setDepartments] = useState([]);
  const [message, setMessage] = useState(null); // { text, type: 'success' | 'danger' }
  const navigate = useNavigate();

  useEffect(() => {
    axios.get("http://localhost:8081/api/departments")
      .then((res) => setDepartments(res.data))
      .catch(() => setDepartments([]));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setMessage(null);

    const payload = {
      fullName,
      username,
      email,
      password,
      role,
      departmentId: departmentId ? parseInt(departmentId) : null,
    };

    try {
      await axios.post("http://localhost:8081/api/auth/register", payload);
      setMessage({ text: "Account created. Redirecting to sign in...", type: "success" });
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      const data = err.response?.data;
      let text = data?.error || "Registration failed.";
      if (data?.fields) {
        text += " " + Object.values(data.fields).join(" ");
      }
      setMessage({ text, type: "danger" });
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="auth-card" style={{ maxWidth: 480 }}>
        <img src="/marca_UJA.png" alt="Universidad de Jaén" style={{ height: 60, marginBottom: 16 }} />
        <h4>Create Account</h4>
        <p className="text-muted">University of Jaén — Purchase Management</p>

        {message && <div className={`alert alert-${message.type}`}>{message.text}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Full name</label>
            <input className="form-control" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Username</label>
            <input className="form-control" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Email</label>
            <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Password</label>
            <input type="password" className="form-control" minLength={6} value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Role</label>
            <select className="form-select" value={role} onChange={(e) => setRole(e.target.value)}>
              <option value="TEACHER">Teacher / Teaching staff</option>
              <option value="EXPENDITURE_UNIT_HEAD">Expenditure Unit Head</option>
              <option value="MANAGEMENT">Management staff</option>
            </select>
          </div>
          <div className="mb-3">
            <label className="form-label">Department</label>
            <select className="form-select" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)} required>
              <option value="">Select a department</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.name}</option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-uja w-100">Register</button>
        </form>

        <div className="text-center mt-3">
          <Link to="/login">Already have an account? Sign in</Link>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;