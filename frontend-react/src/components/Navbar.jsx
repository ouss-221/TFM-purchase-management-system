import { Link } from "react-router-dom";
import { getUsername, getRole, logout } from "../auth.js";

function Navbar() {
  const role = getRole();
  const canSeeStats = ["EXPENDITURE_UNIT_HEAD", "MANAGEMENT", "ADMIN"].includes(role);

  return (
    <nav className="navbar navbar-expand-lg navbar-uja">
      <div className="container-fluid">
        <span className="navbar-brand d-flex align-items-center gap-2">
          <span style={{ background: "#fff", borderRadius: 4, padding: "3px 8px", display: "inline-flex", alignItems: "center" }}>
            <img src="/marca_UJA.png" alt="UJA" style={{ height: 26 }} />
          </span>
          Purchase Management
        </span>

        {canSeeStats && (
          <div className="d-flex gap-2 mx-3">
            <Link to="/statistics" className="btn btn-outline-light btn-sm">Statistics</Link>
            <Link to="/search" className="btn btn-outline-light btn-sm">Search</Link>
          </div>
        )}

        <div className="d-flex align-items-center gap-3">
          <Link
            to="/help"
            title="Help"
            style={{
              display: "inline-flex",
              alignItems: "center",
              justifyContent: "center",
              width: 30,
              height: 30,
              borderRadius: "50%",
              color: "rgba(255,255,255,0.75)",
              transition: "background-color 0.15s, color 0.15s",
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = "rgba(255,255,255,0.15)";
              e.currentTarget.style.color = "#ffffff";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = "transparent";
              e.currentTarget.style.color = "rgba(255,255,255,0.75)";
            }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10" />
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </Link>

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