// Returns the stored Basic Auth token, or null if not logged in
function getAuthToken() {
    return sessionStorage.getItem("authToken");
}

function getUsername() {
    return sessionStorage.getItem("username");
}

function getRole() {
    return sessionStorage.getItem("role");
}

function isLoggedIn() {
    return !!getAuthToken();
}

// Redirect to login if not authenticated - call this at the top of protected pages
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = "/login.html";
    }
}

function logout() {
    sessionStorage.clear();
    window.location.href = "/login.html";
}

// Standard AJAX headers for every authenticated API call
function authHeaders() {
    return { "Authorization": "Basic " + getAuthToken() };
}

// Shared error handler
function handleAjaxError(request) {
    let msg = "An error occurred.";
    if (request.responseJSON && request.responseJSON.error) {
        msg = request.responseJSON.error;
        if (request.responseJSON.fields) {
            const fields = request.responseJSON.fields;
            msg += ":\n" + Object.keys(fields).map(k => "- " + k + ": " + fields[k]).join("\n");
        }
    } else if (request.status === 403) {
        msg = "You don't have permission to do that.";
    } else if (request.status === 401) {
        msg = "Session expired. Please log in again.";
        logout();
        return;
    }
    alert(msg);
}