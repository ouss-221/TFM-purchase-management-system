import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api.js";
import { isLoggedIn, getUsername } from "../auth.js";import Navbar from "../components/Navbar.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import OrderForm from "../components/OrderForm.jsx";
import AttachmentsModal from "../components/AttachmentsModal.jsx";

function DashboardPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const [expenditureUnits, setExpenditureUnits] = useState([]);

  const [showForm, setShowForm] = useState(false);
  const [editingOrder, setEditingOrder] = useState(null);

  const [attachmentsOrderId, setAttachmentsOrderId] = useState(null);

  useEffect(() => {
    if (!isLoggedIn()) {
      navigate("/login");
    }
  }, [navigate]);

  const loadOrders = useCallback(() => {
    setLoading(true);
    api.get("/purchase-orders")
      .then((response) => setOrders(response.data))
      .catch((err) => console.error("Failed to load orders:", err))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadOrders();
    api.get("/expenditure-units").then((r) => setExpenditureUnits(r.data)).catch(console.error);
  }, [loadOrders]);

  function handleStatusChange(id, status) {
    let confirmMessage;
    if (status === "APPROVED") {
      const name = getUsername();
      confirmMessage = `You are about to digitally sign and approve this purchase order as ${name}. Your personal signature will be added to the document. Continue?`;
    } else {
      confirmMessage = `Change order status to ${status}?`;
    }
    if (!window.confirm(confirmMessage)) return;

    api.put(`/purchase-orders/${id}/status?status=${status}`)
      .then(loadOrders)
      .catch((err) => alert(err.response?.data?.error || "Action failed."));
  }

  function handleDelete(id) {
    if (!window.confirm("Delete this order permanently?")) return;
    api.delete(`/purchase-orders/${id}`)
      .then(loadOrders)
      .catch((err) => alert(err.response?.data?.error || "Delete failed."));
  }

  function handleEdit(id) {
    const order = orders.find((o) => o.id === id);
    setEditingOrder(order);
    setShowForm(true);
  }

  function handleNewOrder() {
    setEditingOrder(null);
    setShowForm(true);
  }

  function handleAttachments(id) {
    setAttachmentsOrderId(id);
  }

  function handleDownloadSigned(id, orderNumber) {
    api.get(`/purchase-orders/${id}/signed-document`, { responseType: "blob" })
      .then((response) => {
        const url = URL.createObjectURL(response.data);
        const a = document.createElement("a");
        a.href = url;
        a.download = `order-${orderNumber}-signed.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      })
      .catch((err) => {
        alert(err.response?.data?.error || "Could not generate the signed document.");
      });
  }

  function handleSaveOrder(payload, editingId) {
    const request = editingId
      ? api.put(`/purchase-orders/${editingId}`, payload)
      : api.post("/purchase-orders", payload);

    request
      .then(() => {
        setShowForm(false);
        setEditingOrder(null);
        loadOrders();
      })
      .catch((err) => {
        const data = err.response?.data;
        let msg = data?.error || "Save failed.";
        if (data?.fields) {
          msg += "\n" + Object.entries(data.fields).map(([k, v]) => `- ${k}: ${v}`).join("\n");
        }
        alert(msg);
      });
  }

  return (
    <div>
      <Navbar />
      <div className="container my-4">
        <div className="uja-card">
          <div className="d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Purchase Orders</h5>
            {!showForm && (
              <button className="btn btn-uja btn-sm" onClick={handleNewOrder}>
                + New Order
              </button>
            )}
          </div>
          <div className="mt-3">
            <OrdersTable
              orders={orders}
              loading={loading}
              onStatusChange={handleStatusChange}
              onDelete={handleDelete}
              onEdit={handleEdit}
              onAttachments={handleAttachments}
              onDownloadSigned={handleDownloadSigned}
            />
          </div>
        </div>

        <OrderForm
          show={showForm}
          onClose={() => { setShowForm(false); setEditingOrder(null); }}
          onSave={handleSaveOrder}
          expenditureUnits={expenditureUnits}
          editingOrder={editingOrder}
        />
      </div>

      {attachmentsOrderId && (
        <AttachmentsModal
          orderId={attachmentsOrderId}
          onClose={() => setAttachmentsOrderId(null)}
        />
      )}
    </div>
  );
}

export default DashboardPage;