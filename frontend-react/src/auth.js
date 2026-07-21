export function isLoggedIn() {
    return !!sessionStorage.getItem("authToken");
}

export function getUsername() {
    return sessionStorage.getItem("username");
}

export function getRole() {
    return sessionStorage.getItem("role");
}

export function logout() {
    sessionStorage.clear();
    window.location.href = "/login";
}