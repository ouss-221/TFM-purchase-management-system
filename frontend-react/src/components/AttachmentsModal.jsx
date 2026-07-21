import { useState, useEffect } from "react";
import api from "../api.js";

function AttachmentsModal({ orderId, onClose }) {
  const [attachments, setAttachments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");

  function loadAttachments() {
    setLoading(true);
    api.get(`/purchase-orders/${orderId}/attachments`)
      .then((res) => setAttachments(res.data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadAttachments();
  }, [orderId]);

  function handleUpload() {
    if (!file) {
      setError("Choose a file first.");
      return;
    }
    setError("");
    setUploading(true);

    const formData = new FormData();
    formData.append("file", file);

    api.post(`/purchase-orders/${orderId}/attachments`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    })
      .then(() => {
        setFile(null);
        document.getElementById("attachmentFileInput").value = "";
        loadAttachments();
      })
      .catch((err) => {
        setError(err.response?.data?.error || "Upload failed.");
      })
      .finally(() => setUploading(false));
  }

  function handleDownload(id, fileName) {
    api.get(`/attachments/${id}/download`, { responseType: "blob" })
      .then((res) => {
        const url = URL.createObjectURL(res.data);
        const a = document.createElement("a");
        a.href = url;
        a.download = fileName;
        a.click();
        URL.revokeObjectURL(url);
      })
      .catch(() => alert("Could not download the file."));
  }

  return (
    <div
      className="modal d-block"
      style={{ backgroundColor: "rgba(0,0,0,0.5)" }}
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Attachments</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            {error && <div className="alert alert-danger">{error}</div>}

            {loading ? (
              <p className="text-muted">Loading...</p>
            ) : attachments.length === 0 ? (
              <p className="text-muted">No documents attached yet.</p>
            ) : (
              <ul className="list-group mb-3">
                {attachments.map((a) => (
                  <li key={a.id} className="list-group-item d-flex justify-content-between align-items-center">
                    {a.fileName}
                    <button
                      className="btn btn-outline-secondary btn-sm"
                      onClick={() => handleDownload(a.id, a.fileName)}
                    >
                      Download
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <input
              id="attachmentFileInput"
              type="file"
              className="form-control"
              accept=".pdf,.doc,.docx,.png,.jpg,.jpeg"
              onChange={(e) => setFile(e.target.files[0])}
            />
            <button
              className="btn btn-uja btn-sm mt-2"
              onClick={handleUpload}
              disabled={uploading}
            >
              {uploading ? "Uploading..." : "Upload"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AttachmentsModal;