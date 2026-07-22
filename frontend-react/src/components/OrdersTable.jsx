import { useState } from "react";
import OrderActions from "./OrderActions.jsx";
import OrderDetailRow from "./OrderDetailRow.jsx";

function OrdersTable({ orders, loading, onStatusChange, onDelete, onEdit, onAttachments, onDownloadSigned }) {
  const [expandedId, setExpandedId] = useState(null);

  function toggleDetails(id) {
    setExpandedId(expandedId === id ? null : id);
  }

  if (loading) {
    return <p className="text-muted">Loading orders...</p>;
  }

  if (orders.length === 0) {
    return <p className="text-muted">No purchase orders yet.</p>;
  }

  const colSpan = 8;

  return (
    <div className="table-responsive">
      <table className="table uja-table align-middle">
        <thead>
          <tr>
            <th>Order # / Expediente</th>
            <th>Date</th>
            <th>Requested by</th>
            <th>Expenditure Unit</th>
            <th>Period</th>
            <th>Total (incl. VAT)</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => (
            <>
              <tr key={o.id}>
                <td>
                  {o.orderNumber}
                  <div className="text-muted small">{o.fileNumber}</div>
                </td>
                <td>{o.requestDate ? o.requestDate.substring(0, 10) : ""}</td>
                <td>{o.requestedByUsername}</td>
                <td>{o.expenditureUnitName}</td>
                <td>{o.period || ""}</td>
                <td>{o.totalAmount != null ? Number(o.totalAmount).toFixed(2) + " €" : ""}</td>
                <td>
                  <span className={`status-badge status-${o.status}`}>{o.status}</span>
                </td>
                <td>
                  <OrderActions
                    order={o}
                    expanded={expandedId === o.id}
                    onToggleDetails={toggleDetails}
                    onStatusChange={onStatusChange}
                    onDelete={onDelete}
                    onEdit={onEdit}
                    onAttachments={onAttachments}
                    onDownloadSigned={onDownloadSigned}
                  />
                </td>
              </tr>
              {expandedId === o.id && <OrderDetailRow key={o.id + "-detail"} order={o} colSpan={colSpan} />}
            </>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default OrdersTable;