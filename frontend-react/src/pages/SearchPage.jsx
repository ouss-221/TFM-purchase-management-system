import { useState, useEffect } from "react";
import api from "../api.js";
import Navbar from "../components/Navbar.jsx";

function SearchPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [results, setResults] = useState(null); // null = not searched yet
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!searchTerm.trim()) {
      setResults(null);
      return;
    }

    setLoading(true);
    const timer = setTimeout(() => {
      api.get("/purchase-orders/search", { params: { product: searchTerm } })
        .then((res) => setResults(res.data))
        .catch((err) => {
          console.error("Search failed:", err);
          setResults([]);
        })
        .finally(() => setLoading(false));
    }, 300);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  return (
    <div>
      <Navbar />
      <div className="container my-4">
        <div className="uja-card">
          <h5>Search purchases by item</h5>

          <input
            type="text"
            className="form-control my-3"
            placeholder="Start typing a product name (e.g. router)"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />

          {searchTerm.trim() === "" ? (
            <p className="text-muted">Start typing to search.</p>
          ) : loading || results === null ? (
            <p className="text-muted">Searching...</p>
          ) : results.length === 0 ? (
            <p>No matching items found.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Expenditure unit</th>
                  <th>Product</th>
                  <th>Qty</th>
                  <th>Unit price</th>
                  <th>VAT</th>
                  <th>Line total</th>
                  <th>Supplier</th>
                </tr>
              </thead>
              <tbody>
                {results.map((r, i) => (
                  <tr key={i}>
                    <td>{r.orderNumber}</td>
                    <td>{new Date(r.requestDate).toLocaleDateString()}</td>
                    <td>{r.status}</td>
                    <td>{r.expenditureUnitName}</td>
                    <td>{r.productName}</td>
                    <td>{r.quantity}</td>
                    <td>{r.unitPrice.toFixed(2)} €</td>
                    <td>{r.vatRate}%</td>
                    <td>{r.lineTotal.toFixed(2)} €</td>
                    <td>{r.supplier || "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default SearchPage;