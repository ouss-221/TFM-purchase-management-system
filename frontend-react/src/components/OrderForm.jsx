import { useState, useEffect, useRef } from "react";

const VAT_OPTIONS = [
  { label: "21% (General)", value: "21" },
  { label: "10% (Reduced)", value: "10" },
  { label: "4% (Super-reduced)", value: "4" },
  { label: "0% (Exempt)", value: "0" },
  { label: "Other...", value: "custom" },
];

function emptyItem() {
  return {
    productName: "",
    description: "",
    quantity: 1,
    unitPrice: "",
    vatSelect: "21",
    vatCustom: "",
    supplier: "",
  };
}

function OrderForm({ show, onClose, onSave, editingOrder, expenditureUnits }) {
  const [expenditureUnitId, setExpenditureUnitId] = useState("");
  const [unitSearch, setUnitSearch] = useState("");       // what the user types
  const [showUnitList, setShowUnitList] = useState(false); // dropdown open/closed
  const unitBoxRef = useRef(null);

  const [requesterPhone, setRequesterPhone] = useState("");
  const [budgetLineCode, setBudgetLineCode] = useState("");
  const [period, setPeriod] = useState("");
  const [notes, setNotes] = useState("");
  const [deliveryBuilding, setDeliveryBuilding] = useState("");
  const [deliveryRoom, setDeliveryRoom] = useState("");
  const [deliveryPhone, setDeliveryPhone] = useState("");
  const [deliveryContactPerson, setDeliveryContactPerson] = useState("");
  const [items, setItems] = useState([emptyItem()]);

  useEffect(() => {
    if (editingOrder) {
      setExpenditureUnitId(editingOrder.expenditureUnitId);
      // pre-fill the search box with the selected unit's label
      const u = expenditureUnits.find((x) => x.id === editingOrder.expenditureUnitId);
      setUnitSearch(u ? `${u.name} (${u.code})` : "");
      setRequesterPhone(editingOrder.requesterPhone || "");
      setBudgetLineCode(editingOrder.budgetLineCode || "");
      setPeriod(editingOrder.period || "");
      setNotes(editingOrder.notes || "");
      setDeliveryBuilding(editingOrder.deliveryBuilding || "");
      setDeliveryRoom(editingOrder.deliveryRoom || "");
      setDeliveryPhone(editingOrder.deliveryPhone || "");
      setDeliveryContactPerson(editingOrder.deliveryContactPerson || "");
      setItems(
        editingOrder.items.map((it) => {
          const knownRates = ["21", "10", "4", "0"];
          const rateStr = String(Number(it.vatRate));
          const isKnown = knownRates.includes(rateStr);
          return {
            productName: it.productName,
            description: it.description || "",
            quantity: it.quantity,
            unitPrice: it.unitPrice,
            vatSelect: isKnown ? rateStr : "custom",
            vatCustom: isKnown ? "" : rateStr,
            supplier: it.supplier || "",
          };
        })
      );
    } else {
      setExpenditureUnitId("");
      setUnitSearch("");
      setRequesterPhone("");
      setBudgetLineCode("");
      setPeriod("");
      setNotes("");
      setDeliveryBuilding("");
      setDeliveryRoom("");
      setDeliveryPhone("");
      setDeliveryContactPerson("");
      setItems([emptyItem()]);
    }
  }, [editingOrder, show, expenditureUnits]);

  // close the dropdown when clicking outside it
  useEffect(() => {
    function handleClickOutside(e) {
      if (unitBoxRef.current && !unitBoxRef.current.contains(e.target)) {
        setShowUnitList(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  if (!show) return null;

  const filteredUnits = expenditureUnits.filter((u) => {
    const q = unitSearch.toLowerCase();
    return (
      u.name.toLowerCase().includes(q) ||
      (u.code && u.code.toLowerCase().includes(q))
    );
  });

  function selectUnit(u) {
    setExpenditureUnitId(u.id);
    setUnitSearch(`${u.name} (${u.code})`);
    setShowUnitList(false);
  }

  function updateItem(index, field, value) {
    const updated = [...items];
    updated[index] = { ...updated[index], [field]: value };
    setItems(updated);
  }

  function addItem() {
    setItems([...items, emptyItem()]);
  }

  function removeItem(index) {
    setItems(items.filter((_, i) => i !== index));
  }

  function handleSubmit(e) {
    e.preventDefault();

    if (!expenditureUnitId) {
      alert("Please select an expenditure unit from the list.");
      return;
    }

    if (!editingOrder) {
      if (!window.confirm("You are about to digitally sign and submit this purchase order. Your personal signature will be added to the document. Continue?")) {
        return;
      }
    }
    const payload = {
      expenditureUnitId: parseInt(expenditureUnitId),
      requesterPhone: requesterPhone || null,
      budgetLineCode: budgetLineCode || null,
      period: period || null,
      notes: notes || null,
      deliveryBuilding: deliveryBuilding || null,
      deliveryRoom: deliveryRoom || null,
      deliveryPhone: deliveryPhone || null,
      deliveryContactPerson: deliveryContactPerson || null,
      items: items.map((it) => ({
        productName: it.productName,
        description: it.description || null,
        quantity: parseInt(it.quantity),
        unitPrice: parseFloat(it.unitPrice),
        vatRate: parseFloat(it.vatSelect === "custom" ? it.vatCustom : it.vatSelect),
        supplier: it.supplier || null,
      })),
    };

    onSave(payload, editingOrder ? editingOrder.id : null);
  }

  return (
    <div className="uja-card">
      <h5>{editingOrder ? `Edit Order ${editingOrder.orderNumber}` : "New Purchase Order"}</h5>
      <form onSubmit={handleSubmit}>

        <h6>Request details</h6>
        <div className="row">
          <div className="col-md-4 mb-3">
            <label className="form-label">Expenditure Unit (Centro de Gasto)</label>
            <div ref={unitBoxRef} style={{ position: "relative" }}>
              <input
                className="form-control"
                placeholder="Type to search a unit..."
                value={unitSearch}
                onChange={(e) => {
                  setUnitSearch(e.target.value);
                  setExpenditureUnitId(""); // clear selection until they pick a real one
                  setShowUnitList(true);
                }}
                onFocus={() => setShowUnitList(true)}
                autoComplete="off"
              />
              {showUnitList && filteredUnits.length > 0 && (
                <ul
                  className="list-group"
                  style={{
                    position: "absolute",
                    zIndex: 1000,
                    width: "100%",
                    maxHeight: 220,
                    overflowY: "auto",
                    boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
                  }}
                >
                  {filteredUnits.map((u) => (
                    <li
                      key={u.id}
                      className="list-group-item list-group-item-action"
                      style={{ cursor: "pointer" }}
                      onMouseDown={() => selectUnit(u)}
                    >
                      {u.name} <span className="text-muted small">({u.code})</span>
                    </li>
                  ))}
                </ul>
              )}
              {showUnitList && filteredUnits.length === 0 && (
                <ul
                  className="list-group"
                  style={{ position: "absolute", zIndex: 1000, width: "100%" }}
                >
                  <li className="list-group-item text-muted">No matching unit</li>
                </ul>
              )}
            </div>
          </div>
          <div className="col-md-4 mb-3">
            <label className="form-label">Your phone number</label>
            <input
              className="form-control"
              value={requesterPhone}
              onChange={(e) => setRequesterPhone(e.target.value)}
            />
          </div>
          <div className="col-md-4 mb-3">
            <label className="form-label">Budget line (Aplicación presupuestaria)</label>
            <input
              className="form-control"
              value={budgetLineCode}
              onChange={(e) => setBudgetLineCode(e.target.value)}
            />
          </div>
        </div>

        <div className="row">
          <div className="col-md-4 mb-3">
            <label className="form-label">Period</label>
            <input
              className="form-control"
              placeholder="e.g. 2026-Q3"
              value={period}
              onChange={(e) => setPeriod(e.target.value)}
            />
          </div>
          <div className="col-md-8 mb-3">
            <label className="form-label">Notes</label>
            <input
              className="form-control"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>
        </div>

        <h6 className="mt-3">Delivery (Hacer la entrega en)</h6>
        <div className="row">
          <div className="col-md-3 mb-3">
            <label className="form-label">Building</label>
            <input
              className="form-control"
              value={deliveryBuilding}
              onChange={(e) => setDeliveryBuilding(e.target.value)}
            />
          </div>
          <div className="col-md-3 mb-3">
            <label className="form-label">Room / Office</label>
            <input
              className="form-control"
              value={deliveryRoom}
              onChange={(e) => setDeliveryRoom(e.target.value)}
            />
          </div>
          <div className="col-md-3 mb-3">
            <label className="form-label">Delivery phone</label>
            <input
              className="form-control"
              value={deliveryPhone}
              onChange={(e) => setDeliveryPhone(e.target.value)}
            />
          </div>
          <div className="col-md-3 mb-3">
            <label className="form-label">Contact person</label>
            <input
              className="form-control"
              value={deliveryContactPerson}
              onChange={(e) => setDeliveryContactPerson(e.target.value)}
            />
          </div>
        </div>

        <h6 className="mt-3">Line items</h6>
        <table className="table table-sm">
          <thead>
            <tr>
              <th style={{ minWidth: 160 }}>Product (max 150 chars)</th>
              <th style={{ minWidth: 200 }}>Description (max 2000 chars)</th>
              <th style={{ width: 80 }}>Qty</th>
              <th style={{ width: 110 }}>Unit price</th>
              <th style={{ width: 150 }}>VAT</th>
              <th style={{ minWidth: 160 }}>Supplier</th>
              <th style={{ width: 40 }}></th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, index) => (
              <tr key={index}>
                <td>
                  <input
                    className="form-control form-control-sm"
                    maxLength={150}
                    required
                    value={item.productName}
                    onChange={(e) => updateItem(index, "productName", e.target.value)}
                  />
                </td>
                <td>
                  <textarea
                    className="form-control form-control-sm"
                    maxLength={2000}
                    rows={1}
                    value={item.description}
                    onChange={(e) => updateItem(index, "description", e.target.value)}
                  />
                </td>
                <td>
                  <input
                    type="number"
                    min="1"
                    className="form-control form-control-sm"
                    required
                    value={item.quantity}
                    onChange={(e) => updateItem(index, "quantity", e.target.value)}
                  />
                </td>
                <td>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="form-control form-control-sm"
                    required
                    value={item.unitPrice}
                    onChange={(e) => updateItem(index, "unitPrice", e.target.value)}
                  />
                </td>
                <td>
                  <select
                    className="form-select form-select-sm mb-1"
                    value={item.vatSelect}
                    onChange={(e) => updateItem(index, "vatSelect", e.target.value)}
                  >
                    {VAT_OPTIONS.map((opt) => (
                      <option key={opt.value} value={opt.value}>{opt.label}</option>
                    ))}
                  </select>
                  {item.vatSelect === "custom" && (
                    <input
                      type="number"
                      min="0"
                      max="100"
                      step="0.01"
                      className="form-control form-control-sm"
                      placeholder="% VAT"
                      required
                      value={item.vatCustom}
                      onChange={(e) => updateItem(index, "vatCustom", e.target.value)}
                    />
                  )}
                </td>
                <td>
                  <input
                    className="form-control form-control-sm"
                    placeholder="Supplier name"
                    value={item.supplier}
                    onChange={(e) => updateItem(index, "supplier", e.target.value)}
                  />
                </td>
                <td>
                  {items.length > 1 && (
                    <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => removeItem(index)}>
                      ✕
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <button type="button" className="btn btn-outline-secondary btn-sm mb-3" onClick={addItem}>
          + Add item
        </button>

        <div className="d-flex gap-2">
          <button type="submit" className="btn btn-uja">
            {editingOrder ? "Save changes" : "Sign and submit"}
          </button>
          <button type="button" className="btn btn-outline-secondary" onClick={onClose}>Cancel</button>
        </div>
      </form>
    </div>
  );
}

export default OrderForm;