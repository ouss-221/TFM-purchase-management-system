// ======================= STATE =======================
let expenditureUnits = [];
let suppliers = [];
let productTypes = [];
let currentOrders = [];
let attachmentsOrderId = null;

// ======================= INIT =======================
$(document).ready(function () {
    requireAuth();

    // Navbar identity
    $("#navUsername").text(getUsername());
    $("#navRole").text(getRole().replaceAll("_", " "));

    // Role-based section visibility
    const role = getRole();
    if (["EXPENDITURE_UNIT_HEAD", "MANAGEMENT", "ADMIN"].includes(role)) {
        $("#statsCard").removeClass("d-none");
        loadStats("by-supplier");
    }
    if (role === "ADMIN") {
        $("#logsCard").removeClass("d-none");
        loadAccessLogs();
    }

    // Load reference data (units/suppliers/types) then orders
    loadReferenceData();
    loadOrders();

    // Wire up events
    $("#btnNewOrder").click(openNewOrderForm);
    $("#btnCancelOrder").click(closeOrderForm);
    $("#btnAddItem").click(() => addItemRow());
    $("#orderForm").submit(saveOrder);
    $("#statsTabs").on("click", "a", switchStatsTab);
    $("#btnUploadAttachment").click(uploadAttachment);
});

// ======================= REFERENCE DATA =======================
function loadReferenceData() {
    $.ajax({ url: "/api/expenditure-units", headers: authHeaders(), success: d => {
        expenditureUnits = d;
        const sel = $("#orderUnit").empty();
        d.forEach(u => sel.append(`<option value="${u.id}">${u.name} (${u.code})</option>`));
    }, error: handleAjaxError });

    $.ajax({ url: "/api/suppliers", headers: authHeaders(), success: d => suppliers = d, error: handleAjaxError });
    $.ajax({ url: "/api/product-types", headers: authHeaders(), success: d => productTypes = d, error: handleAjaxError });
}

// ======================= ORDERS TABLE =======================
function loadOrders() {
    $.ajax({
        url: "/api/purchase-orders",
        headers: authHeaders(),
        success: function (orders) {
            currentOrders = orders;
            const body = $("#ordersTableBody").empty();
            if (orders.length === 0) {
                body.append('<tr><td colspan="8" class="text-center text-muted">No purchase orders yet.</td></tr>');
                return;
            }
            orders.forEach(o => body.append(orderRow(o)));
        },
        error: handleAjaxError
    });
}

function orderRow(o) {
    const date = o.requestDate ? o.requestDate.substring(0, 10) : "";
    const total = o.totalAmount != null ? Number(o.totalAmount).toFixed(2) + " €" : "";
    return `
        <tr>
            <td>${o.orderNumber}</td>
            <td>${date}</td>
            <td>${o.requestedByUsername}</td>
            <td>${o.expenditureUnitName}</td>
            <td>${o.period || ""}</td>
            <td>${total}</td>
            <td><span class="status-badge status-${o.status}">${o.status}</span></td>
            <td>${actionButtons(o)}</td>
        </tr>`;
}

function actionButtons(o) {
    const role = getRole();
    const me = getUsername();
    let html = "";

    // Attachments: everyone involved can view/upload
    html += `<button class="btn btn-outline-secondary btn-sm me-1" onclick="openAttachments(${o.id})" title="Attachments">📎</button>`;

    // Edit: only the owner (teacher) while PENDING (admin can too)
    const isOwner = o.requestedByUsername === me;
    if (o.status === "PENDING" && (isOwner || role === "ADMIN")) {
        html += `<button class="btn btn-outline-primary btn-sm me-1" onclick="openEditOrderForm(${o.id})">Edit</button>`;
    }

    // Approve / Reject: unit head, management, admin — only while PENDING
    if (o.status === "PENDING" && ["EXPENDITURE_UNIT_HEAD", "MANAGEMENT", "ADMIN"].includes(role)) {
        html += `<button class="btn btn-uja btn-sm me-1" onclick="changeStatus(${o.id}, 'APPROVED')">Approve</button>`;
        html += `<button class="btn btn-outline-danger btn-sm me-1" onclick="changeStatus(${o.id}, 'REJECTED')">Reject</button>`;
    }

    // Further lifecycle: management/admin can move approved orders forward
    if (o.status === "APPROVED" && ["MANAGEMENT", "ADMIN"].includes(role)) {
        html += `<button class="btn btn-outline-info btn-sm me-1" onclick="changeStatus(${o.id}, 'PROCESSED')">Mark processed</button>`;
    }
    if (o.status === "PROCESSED" && ["MANAGEMENT", "ADMIN"].includes(role)) {
        html += `<button class="btn btn-outline-dark btn-sm me-1" onclick="changeStatus(${o.id}, 'DELIVERED')">Mark delivered</button>`;
    }

    // Delete: management/admin
    if (["MANAGEMENT", "ADMIN"].includes(role)) {
        html += `<button class="btn btn-outline-danger btn-sm" onclick="deleteOrder(${o.id})">Delete</button>`;
    }
    return html;
}

function changeStatus(id, status) {
    if (!confirm(`Change order status to ${status}?`)) return;
    $.ajax({
        url: `/api/purchase-orders/${id}/status?status=${status}`,
        type: "PUT",
        headers: authHeaders(),
        success: loadOrders,
        error: handleAjaxError
    });
}

function deleteOrder(id) {
    if (!confirm("Delete this order permanently?")) return;
    $.ajax({
        url: `/api/purchase-orders/${id}`,
        type: "DELETE",
        headers: authHeaders(),
        success: loadOrders,
        error: handleAjaxError
    });
}

// ======================= ORDER FORM (create/edit) =======================
function openNewOrderForm() {
    $("#orderFormTitle").text("New Purchase Order");
    $("#editingOrderId").val("");
    $("#orderForm")[0].reset();
    $("#itemsTableBody").empty();
    addItemRow(); // start with one empty row
    $("#orderFormCard").removeClass("d-none");
    window.scrollTo({ top: $("#orderFormCard").offset().top - 20, behavior: "smooth" });
}

function openEditOrderForm(id) {
    const o = currentOrders.find(x => x.id === id);
    if (!o) return;
    $("#orderFormTitle").text("Edit Order " + o.orderNumber);
    $("#editingOrderId").val(o.id);
    $("#orderUnit").val(o.expenditureUnitId);
    $("#orderPeriod").val(o.period || "");
    $("#orderNotes").val(o.notes || "");
    $("#itemsTableBody").empty();
    o.items.forEach(it => addItemRow(it));
    $("#orderFormCard").removeClass("d-none");
    window.scrollTo({ top: $("#orderFormCard").offset().top - 20, behavior: "smooth" });
}

function closeOrderForm() {
    $("#orderFormCard").addClass("d-none");
}

function addItemRow(item) {
    item = item || {};
    const typeOptions = productTypes.map(t =>
        `<option value="${t.id}" ${item.productTypeId === t.id ? "selected" : ""}>${t.name}</option>`).join("");
    const supplierOptions = suppliers.map(s =>
        `<option value="${s.id}" ${item.supplierId === s.id ? "selected" : ""}>${s.name}</option>`).join("");

    $("#itemsTableBody").append(`
        <tr>
            <td><input class="form-control form-control-sm it-name" required value="${item.productName || ""}"></td>
            <td><input class="form-control form-control-sm it-desc" value="${item.description || ""}"></td>
            <td><input type="number" min="1" class="form-control form-control-sm it-qty" required value="${item.quantity || 1}"></td>
            <td><input type="number" min="0.01" step="0.01" class="form-control form-control-sm it-price" required value="${item.unitPrice || ""}"></td>
            <td><select class="form-select form-select-sm it-type"><option value="">—</option>${typeOptions}</select></td>
            <td><select class="form-select form-select-sm it-supplier"><option value="">—</option>${supplierOptions}</select></td>
            <td><button type="button" class="btn btn-outline-danger btn-sm" onclick="$(this).closest('tr').remove()">✕</button></td>
        </tr>`);
}

function saveOrder(e) {
    e.preventDefault();

    const items = [];
    $("#itemsTableBody tr").each(function () {
        items.push({
            productName: $(this).find(".it-name").val(),
            description: $(this).find(".it-desc").val() || null,
            quantity: parseInt($(this).find(".it-qty").val()),
            unitPrice: parseFloat($(this).find(".it-price").val()),
            productTypeId: parseInt($(this).find(".it-type").val()) || null,
            supplierId: parseInt($(this).find(".it-supplier").val()) || null
        });
    });

    const payload = {
        expenditureUnitId: parseInt($("#orderUnit").val()),
        period: $("#orderPeriod").val() || null,
        notes: $("#orderNotes").val() || null,
        items: items
    };

    const editingId = $("#editingOrderId").val();
    const isEdit = editingId !== "";

    $.ajax({
        url: isEdit ? `/api/purchase-orders/${editingId}` : "/api/purchase-orders",
        type: isEdit ? "PUT" : "POST",
        headers: authHeaders(),
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(payload),
        success: function () {
            closeOrderForm();
            loadOrders();
        },
        error: handleAjaxError
    });
}

// ======================= ATTACHMENTS =======================
function openAttachments(orderId) {
    attachmentsOrderId = orderId;
    refreshAttachmentsList();
    new bootstrap.Modal(document.getElementById("attachmentsModal")).show();
}

function refreshAttachmentsList() {
    $.ajax({
        url: `/api/purchase-orders/${attachmentsOrderId}/attachments`,
        headers: authHeaders(),
        success: function (list) {
            const ul = $("#attachmentsList").empty();
            if (list.length === 0) {
                ul.append('<li class="list-group-item text-muted">No documents attached.</li>');
                return;
            }
            list.forEach(a => ul.append(`
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    ${a.fileName}
                    <button class="btn btn-outline-secondary btn-sm" onclick="downloadAttachment(${a.id}, '${a.fileName}')">Download</button>
                </li>`));
        },
        error: handleAjaxError
    });
}

function uploadAttachment() {
    const fileInput = document.getElementById("attachmentFile");
    if (!fileInput.files.length) { alert("Choose a file first."); return; }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    $.ajax({
        url: `/api/purchase-orders/${attachmentsOrderId}/attachments`,
        type: "POST",
        headers: authHeaders(),
        data: formData,
        processData: false,
        contentType: false,
        success: function () {
            fileInput.value = "";
            refreshAttachmentsList();
        },
        error: handleAjaxError
    });
}

function downloadAttachment(id, fileName) {
    fetch(`/api/attachments/${id}/download`, { headers: authHeaders() })
        .then(r => { if (!r.ok) throw new Error("Download failed"); return r.blob(); })
        .then(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = fileName;
            a.click();
            URL.revokeObjectURL(url);
        })
        .catch(() => alert("Could not download the file."));
}

// ======================= STATISTICS =======================
function switchStatsTab(e) {
    e.preventDefault();
    $("#statsTabs .nav-link").removeClass("active");
    $(e.target).addClass("active");
    loadStats($(e.target).data("stat"));
}

function loadStats(which) {
    $.ajax({
        url: `/api/statistics/${which}`,
        headers: authHeaders(),
        success: function (rows) {
            const body = $("#statsTableBody").empty();
            if (rows.length === 0) {
                body.append('<tr><td colspan="3" class="text-muted">No data.</td></tr>');
                return;
            }
            rows.forEach(r => body.append(
                `<tr><td>${r.label || "(unassigned)"}</td><td>${Number(r.totalAmount).toFixed(2)}</td><td>${r.orderCount}</td></tr>`));
        },
        error: handleAjaxError
    });
}

// ======================= ADMIN LOGS =======================
function loadAccessLogs() {
    $.ajax({
        url: "/api/access-logs",
        headers: authHeaders(),
        success: function (logs) {
            const body = $("#logsTableBody").empty();
            logs.slice().reverse().forEach(l => body.append(`
                <tr>
                    <td>${l.timestamp ? l.timestamp.replace("T", " ").substring(0, 19) : ""}</td>
                    <td>${l.username}</td>
                    <td>${l.action}</td>
                    <td>${l.entity || ""}</td>
                    <td>${l.entityId || ""}</td>
                </tr>`));
        },
        error: handleAjaxError
    });
}