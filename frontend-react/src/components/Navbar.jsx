import { Link } from "react-router-dom";
import { getUsername, getRole, logout } from "../auth.js";

function Navbar() {
  const role = getRole();

  return (
    <nav className="navbar navbar-expand-lg navbar-uja">
      <div className="container-fluid">
        <span className="navbar-brand d-flex align-items-center gap-2">
          <span style={{ background: "#fff", borderRadius: 4, padding: "3px 8px", display: "inline-flex", alignItems: "center" }}>
            <img src="/marca_UJA.png" alt="UJA" style={{ height: 26 }} />
          </span>
          Purchase Management
        </span>

        {["EXPENDITURE_UNIT_HEAD", "MANAGEMENT", "ADMIN"].includes(role) && (
          <Link to="/statistics" className="btn btn-outline-light btn-sm mx-3">
            Statistics
          </Link>
        )}

        <div className="d-flex align-items-center gap-3">
          <span className="text-white-50">{getUsername()}</span>
          <span className="badge-role">{getRole()?.replaceAll("_", " ")}</span>
          <button className="btn btn-outline-light btn-sm" onClick={logout}>
            Sign out
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;