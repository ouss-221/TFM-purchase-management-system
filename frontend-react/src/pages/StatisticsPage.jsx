import React from "react";
import { useState, useEffect } from "react";
import api from "../api.js";
import Navbar from "../components/Navbar.jsx";

const GROUPINGS = [
  { key: "by-supplier", label: "By supplier" },
  { key: "by-expenditure-unit", label: "By expenditure unit" },
  { key: "by-period", label: "By period" },
];

function num(value) {
  return (value ?? 0).toFixed(2);
}

function StatisticsPage() {
  const [grouping, setGrouping] = useState("by-supplier");
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);

  const [expandedLabel, setExpandedLabel] = useState(null);
  const [drillDown, setDrillDown] = useState([]);
  const [drillLoading, setDrillLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    setExpandedLabel(null);
    api.get(`/statistics/${grouping}`)
      .then((res) => setRows(res.data))
      .catch((err) => console.error("Failed to load statistics:", err))
      .finally(() => setLoading(false));
  }, [grouping]);

  function toggleRow(label) {
    if (expandedLabel === label) {
      setExpandedLabel(null);
      return;
    }
    setExpandedLabel(label);
    setDrillLoading(true);

    const request =
      grouping === "by-supplier"
        ? api.get("/purchase-orders/by-supplier-items", { params: { supplier: label } })
        : api.get("/purchase-orders/by-group", { params: { type: grouping, value: label } });

    request
      .then((res) => setDrillDown(res.data))
      .catch((err) => {
        console.error("Failed to load details:", err);
        setDrillDown([]);
      })
      .finally(() => setDrillLoading(false));
  }

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
                    <React.Fragment key={i}>
                      <tr
                        onClick={() => toggleRow(r.label)}
                        style={{ cursor: "pointer" }}
                        className={expandedLabel === r.label ? "table-active" : ""}
                      >
                        <td>{expandedLabel === r.label ? "▾ " : "▸ "}{r.label}</td>
                        <td>{num(r.totalAmount)} €</td>
                        <td>{r.orderCount ?? 0}</td>
                      </tr>

                      {expandedLabel === r.label && (
                        <tr>
                          <td colSpan="3" className="bg-light">
                            {drillLoading ? (
                              <p className="text-muted mb-0">Loading details...</p>
                            ) : drillDown.length === 0 ? (
                              <p className="text-muted mb-0">No details found.</p>
                            ) : grouping === "by-supplier" ? (
                              <table className="table table-sm mb-0">
                                <thead>
                                  <tr>
                                    <th>Order</th><th>Date</th><th>Product</th>
                                    <th>Qty</th><th>Unit price</th><th>VAT</th><th>Line total</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  {drillDown.map((item, j) => (
                                    <tr key={j}>
                                      <td>{item.orderNumber}</td>
                                      <td>{item.requestDate ? new Date(item.requestDate).toLocaleDateString() : "-"}</td>
                                      <td>{item.productName}</td>
                                      <td>{item.quantity ?? 0}</td>
                                      <td>{num(item.unitPrice)} €</td>
                                      <td>{item.vatRate ?? 0}%</td>
                                      <td>{num(item.lineTotal)} €</td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            ) : (
                              <table className="table table-sm mb-0">
                                <thead>
                                  <tr>
                                    <th>Order #</th><th>Date</th><th>Requested by</th>
                                    <th>Status</th><th>Total</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  {drillDown.map((o) => (
                                    <tr key={o.id}>
                                      <td>{o.orderNumber}</td>
                                      <td>{o.requestDate ? new Date(o.requestDate).toLocaleDateString() : "-"}</td>
                                      <td>{o.requestedByUsername}</td>
                                      <td>{o.status}</td>
                                      <td>{num(o.totalAmount)} €</td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            )}
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
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