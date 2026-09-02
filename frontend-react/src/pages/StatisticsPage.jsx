import { useState, useEffect } from "react";
import api from "../api.js";
import Navbar from "../components/Navbar.jsx";

const GROUPINGS = [
  { key: "by-supplier", label: "By supplier" },
  { key: "by-expenditure-unit", label: "By expenditure unit" },
  { key: "by-period", label: "By period" },
];

function StatisticsPage() {
  const [grouping, setGrouping] = useState("by-supplier");
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    api.get(`/statistics/${grouping}`)
      .then((res) => setRows(res.data))
      .catch((err) => console.error("Failed to load statistics:", err))
      .finally(() => setLoading(false));
  }, [grouping]);

  return (
    <div>
      <Navbar />
      <div className="container my-4">
        <div className="uja-card">
          <h5>Spending Statistics</h5>

          <div className="btn-group my-3">
            {GROUPINGS.map((g) => (
              <button
                key={g.key}
                className={`btn btn-sm ${grouping === g.key ? "btn-uja" : "btn-outline-secondary"}`}
                onClick={() => setGrouping(g.key)}
              >
                {g.label}
              </button>
            ))}
          </div>

          {loading ? (
            <p>Loading...</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>{GROUPINGS.find((g) => g.key === grouping)?.label}</th>
                  <th>Total (incl. VAT)</th>
                  <th>Orders</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr><td colSpan="3">No data yet.</td></tr>
                ) : (
                  rows.map((r, i) => (
                    <tr key={i}>
                      <td>{r.label}</td>
                      <td>{r.totalAmount.toFixed(2)} €</td>
                      <td>{r.orderCount}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default StatisticsPage;