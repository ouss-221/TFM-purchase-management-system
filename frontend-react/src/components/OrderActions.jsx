import { getUsername, getRole } from "../auth.js";

function OrderActions({ order, expanded, onToggleDetails, onStatusChange, onDelete, onEdit, onAttachments }) {
  const role = getRole();
  const me = getUsername();
  const isOwner = order.requestedByUsername === me;

  return (
    <div className="d-flex gap-1 flex-wrap">
      <button className="btn btn-outline-secondary btn-sm" onClick={() => onToggleDetails(order.id)}>
        {expanded ? "Hide details ▲" : "Details ▾"}

      </button>

      <button
        className="btn btn-outline-secondary btn-sm"
        onClick={() => onAttachments(order.id)}
      >
        Files
      </button>

      {order.status === "PENDING" && (isOwner || role === "ADMIN") && (
        <button className="btn btn-outline-primary btn-sm" onClick={() => onEdit(order.id)}>
          Edit
        </button>
      )}

      {order.status === "PENDING" && ["EXPENDITURE_UNIT_HEAD", "MANAGEMENT", "ADMIN"].includes(role) && (
        <>
          <button className="btn btn-uja btn-sm" onClick={() => onStatusChange(order.id, "APPROVED")}>
            Approve
          </button>
          <button className="btn btn-outline-danger btn-sm" onClick={() => onStatusChange(order.id, "REJECTED")}>
            Reject
          </button>
        </>
      )}

      {order.status === "APPROVED" && ["MANAGEMENT", "ADMIN"].includes(role) && (
        <button className="btn btn-outline-info btn-sm" onClick={() => onStatusChange(order.id, "PROCESSED")}>
          Mark processed
        </button>
      )}

      {order.status === "PROCESSED" && ["MANAGEMENT", "ADMIN"].includes(role) && (
        <button className="btn btn-outline-dark btn-sm" onClick={() => onStatusChange(order.id, "DELIVERED")}>
          Mark delivered
        </button>
      )}

      {["MANAGEMENT", "ADMIN"].includes(role) && (
        <button className="btn btn-outline-danger btn-sm" onClick={() => onDelete(order.id)}>
          Delete
        </button>
      )}
    </div>
  );
}

export default OrderActions;