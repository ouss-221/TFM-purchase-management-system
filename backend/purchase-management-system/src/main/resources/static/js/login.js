$(document).ready(function () {
    // If already logged in, skip straight to dashboard
    if (isLoggedIn()) {
        window.location.href = "/dashboard.html";
        return;
    }

    $("#loginForm").submit(function (e) {
        e.preventDefault();
        const username = $("#username").val();
        const password = $("#password").val();
        const token = btoa(username + ":" + password);

        $.ajax({
            url: "/api/auth/me",
            type: "GET",
            headers: { "Authorization": "Basic " + token },
            success: function (data) {
                sessionStorage.setItem("authToken", token);
                sessionStorage.setItem("username", data.username);
                sessionStorage.setItem("role", data.role);
                window.location.href = "/dashboard.html";
            },
            error: function () {
                $("#errorMsg").removeClass("d-none").text("Incorrect username or password.");
            }
        });
    });
});