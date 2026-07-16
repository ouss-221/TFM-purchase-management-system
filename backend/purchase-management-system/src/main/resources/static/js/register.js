$(document).ready(function () {

    loadDepartments();

    $("#registerForm").submit(function (e) {
        e.preventDefault();

        const payload = {
            fullName: $("#fullName").val(),
            username: $("#username").val(),
            email: $("#email").val(),
            password: $("#password").val(),
            role: $("#role").val(),
            departmentId: parseInt($("#departmentId").val()) || null
        };

        $.ajax({
            url: "/api/auth/register",
            type: "POST",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function () {
                showMsg("Account created. Redirecting to sign in...", "success");
                setTimeout(() => window.location.href = "/login.html", 1500);
            },
            error: function (request) {
                let msg = "Registration failed.";
                if (request.responseJSON && request.responseJSON.error) {
                    msg = request.responseJSON.error;
                    if (request.responseJSON.fields) {
                        const f = request.responseJSON.fields;
                        msg += " " + Object.values(f).join(". ");
                    }
                }
                showMsg(msg, "danger");
            }
        });
    });

    function showMsg(text, type) {
        $("#msgBox")
            .removeClass("d-none alert-success alert-danger")
            .addClass("alert-" + type)
            .text(text);
    }

    function loadDepartments() {
        // Note: /api/departments GET requires auth, so for the public register page
        // we can't call it directly. We hardcode a fallback and try the API anyway
        // in case the user has a session.
        $.ajax({
            url: "/api/departments",
            type: "GET",
            headers: isLoggedIn() ? authHeaders() : {},
            success: function (departments) {
                const sel = $("#departmentId").empty();
                sel.append('<option value="">Select a department</option>');
                departments.forEach(d => sel.append(`<option value="${d.id}">${d.name}</option>`));
            },
            error: function () {
                // Not logged in => can't list departments; show a neutral placeholder
                $("#departmentId").empty()
                    .append('<option value="">(Department assigned by admin later)</option>');
            }
        });
    }
});