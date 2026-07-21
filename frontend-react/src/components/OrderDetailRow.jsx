function OrderDetailRow({ order, colSpan }) {
  return (
    <tr>
      <td colSpan={colSpan} className="bg-light">
        <div className="p-3">
          <div className="row mb-2">
            <div className="col-md-3"><strong>Expediente:</strong> {order.fileNumber || "—"}</div>
            <div className="col-md-3"><strong>Requester phone:</strong> {order.requesterPhone || "—"}</div>
            <div className="col-md-3"><strong>Budget line:</strong> {order.budgetLineCode || "—"}</div>
            <div className="col-md-3"><strong>Centro de Gasto code:</strong> {order.expenditureUnitCode || "—"}</div>
          </div>
          <div className="row mb-2">
            <div className="col-md-3"><strong>Delivery building:</strong> {order.deliveryBuilding || "—"}</div>
            <div className="col-md-3"><strong>Delivery room:</strong> {order.deliveryRoom || "—"}</div>
            <div className="col-md-3"><strong>Delivery phone:</strong> {order.deliveryPhone || "—"}</div>
            <div className="col-md-3"><strong>Contact person:</strong> {order.deliveryContactPerson || "—"}</div>
          </div>
          <div className="mb-2"><strong>Notes:</strong> {order.notes || <span className="text-muted">None</span>}</div>

          <table className="table table-sm table-bordered bg-white mt-2 mb-0">
            <thead>
              <tr>
                <th>Product</th>
                <th>Description</th>
                <th>Qty</th>
                <th>Unit price</th>
                <th>VAT</th>
                <th>Line total</th>
                <th>Supplier</th>
              </tr>
            </thead>
            <tbody>
              {order.items.map((item) => (
                <tr key={item.id}>
                  <td>{item.productName}</td>
                  <td>{item.description || ""}</td>
                  <td>{item.quantity}</td>
                  <td>{Number(item.unitPrice).toFixed(2)} €</td>
                  <td>{Number(item.vatRate).toFixed(0)}%</td>
                  <td><strong>{Number(item.lineTotal).toFixed(2)} €</strong></td>
                  <td>{item.supplier || ""}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </td>
    </tr>
  );
}

export default OrderDetailRow;